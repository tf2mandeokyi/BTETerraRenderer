package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import lombok.Getter;
import org.joml.Matrix4d;
import org.joml.Vector3d;

public class SpheroidFrustum {
    private final Plane[] planes;
    @Getter
    private final Vector3d cameraPosition;

    /**
     * Constructs a SpheroidFrustum by defining its six planes based on the camera parameters.
     *
     * @param cameraPosition The camera's position in world space.
     * @param viewDirection  The direction the camera is looking towards (should be normalized).
     * @param rightDirection The direction of the camera's right vector (should be normalized).
     * @param horizontalFov  The horizontal field of view in radians.
     * @param verticalFov    The vertical field of view in radians.
     * @param near           The distance to the near clipping plane.
     * @param far            The distance to the far clipping plane.
     */
    public SpheroidFrustum(Vector3d cameraPosition, Vector3d viewDirection, Vector3d rightDirection,
                           float horizontalFov, float verticalFov, float near, float far) {
        this.cameraPosition = cameraPosition;
        Vector3d z = viewDirection.normalize(new Vector3d());
        Vector3d x = rightDirection.normalize(new Vector3d());
        Vector3d y = x.cross(z, new Vector3d()).normalize();

        float halfHFov = horizontalFov / 2;
        float halfVFov = verticalFov / 2;
        float sinHalfHFov = (float) Math.sin(halfHFov);
        float sinHalfVFov = (float) Math.sin(halfVFov);
        float cosHalfHFov = (float) Math.cos(halfHFov);
        float cosHalfVFov = (float) Math.cos(halfVFov);

        Vector3d zSinHalfHFov = z.mul(sinHalfHFov, new Vector3d());
        Vector3d zSinHalfVFov = z.mul(sinHalfVFov, new Vector3d());
        Vector3d leftNormal = x.mul(cosHalfHFov, new Vector3d()).add(zSinHalfHFov).normalize();
        Vector3d rightNormal = x.mul(-cosHalfHFov, new Vector3d()).add(zSinHalfHFov).normalize();
        Vector3d topNormal = y.mul(-cosHalfVFov, new Vector3d()).add(zSinHalfVFov).normalize();
        Vector3d bottomNormal = y.mul(cosHalfVFov, new Vector3d()).add(zSinHalfVFov).normalize();

        Vector3d zNear = z.mul(near, new Vector3d());
        Vector3d zFar = z.mul(far, new Vector3d());
        Vector3d zNeg = z.negate(new Vector3d());
        Vector3d nearCenter = cameraPosition.add(zNear, new Vector3d());
        Vector3d farCenter = cameraPosition.add(zFar, new Vector3d());

        this.planes = new Plane[6];
        this.planes[0] = new Plane(nearCenter, z); // Near plane
        this.planes[1] = new Plane(farCenter, zNeg); // Far plane
        this.planes[2] = new Plane(cameraPosition, leftNormal); // Left plane
        this.planes[3] = new Plane(cameraPosition, rightNormal); // Right plane
        this.planes[4] = new Plane(cameraPosition, topNormal); // Top plane
        this.planes[5] = new Plane(cameraPosition, bottomNormal); // Bottom plane
    }

    public boolean intersectsVolume(Volume volume, Matrix4d volumeTransform, SpheroidCoordinatesConverter converter) {
        return volume.intersectsPositiveSides(this.planes, volumeTransform, converter);
    }
}