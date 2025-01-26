package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import lombok.Getter;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
                           double horizontalFov, double verticalFov, @Nullable Double near, @Nullable Double far) {
        this.cameraPosition = cameraPosition;
        Vector3d z = viewDirection.normalize(new Vector3d());
        Vector3d x = rightDirection.normalize(new Vector3d());
        Vector3d y = x.cross(z, new Vector3d()).normalize();

        double halfHFov = horizontalFov / 2;
        double halfVFov = verticalFov / 2;
        double sinHalfHFov = Math.sin(halfHFov);
        double sinHalfVFov = Math.sin(halfVFov);
        double cosHalfHFov = Math.cos(halfHFov);
        double cosHalfVFov = Math.cos(halfVFov);

        Vector3d zSinHalfHFov = z.mul(sinHalfHFov, new Vector3d());
        Vector3d zSinHalfVFov = z.mul(sinHalfVFov, new Vector3d());
        Vector3d leftNormal = x.mul(cosHalfHFov, new Vector3d()).add(zSinHalfHFov).normalize();
        Vector3d rightNormal = x.mul(-cosHalfHFov, new Vector3d()).add(zSinHalfHFov).normalize();
        Vector3d topNormal = y.mul(-cosHalfVFov, new Vector3d()).add(zSinHalfVFov).normalize();
        Vector3d bottomNormal = y.mul(cosHalfVFov, new Vector3d()).add(zSinHalfVFov).normalize();

        List<Plane> planes = new ArrayList<>(6);
        planes.add(new Plane(cameraPosition, leftNormal)); // Left plane
        planes.add(new Plane(cameraPosition, rightNormal)); // Right plane
        planes.add(new Plane(cameraPosition, topNormal)); // Top plane
        planes.add(new Plane(cameraPosition, bottomNormal)); // Bottom plane

        if (near != null) {
            Vector3d zNear = z.mul(near, new Vector3d());
            Vector3d nearCenter = cameraPosition.add(zNear, new Vector3d());
            planes.add(new Plane(nearCenter, z)); // Near plane
        }
        if (far != null) {
            Vector3d zNeg = z.negate(new Vector3d());
            Vector3d zFar = z.mul(far, new Vector3d());
            Vector3d farCenter = cameraPosition.add(zFar, new Vector3d());
            planes.add(new Plane(farCenter, zNeg)); // Far plane
        }

        this.planes = planes.toArray(new Plane[0]);
    }

    public boolean intersectsVolume(Volume volume, Matrix4d volumeTransform, SpheroidCoordinatesConverter converter) {
        return volume.intersectsPositiveSides(this.planes, volumeTransform, converter);
    }
}