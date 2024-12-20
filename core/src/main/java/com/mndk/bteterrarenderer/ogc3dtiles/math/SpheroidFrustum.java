package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;

public class SpheroidFrustum {
    private final Plane[] planes;

    /**
     * Constructs a SpheroidFrustum by defining its six planes based on the camera parameters.
     *
     * @param point          The camera's position in world space.
     * @param viewDirection  The direction the camera is looking towards (should be normalized).
     * @param rightDirection The direction of the camera's right vector (should be normalized).
     * @param horizontalFov  The horizontal field of view in radians.
     * @param verticalFov    The vertical field of view in radians.
     * @param near           The distance to the near clipping plane.
     * @param far            The distance to the far clipping plane.
     */
    public SpheroidFrustum(Cartesian3f point, Cartesian3f viewDirection, Cartesian3f rightDirection,
                           float horizontalFov, float verticalFov, float near, float far) {
        Cartesian3f z = viewDirection.toNormalized();
        Cartesian3f x = rightDirection.toNormalized();
        Cartesian3f y = x.cross(z).toNormalized();

        float halfHFov = horizontalFov / 2;
        float halfVFov = verticalFov / 2;
        float sinHalfHFov = (float) Math.sin(halfHFov);
        float sinHalfVFov = (float) Math.sin(halfVFov);
        float cosHalfHFov = (float) Math.cos(halfHFov);
        float cosHalfVFov = (float) Math.cos(halfVFov);

        Cartesian3f leftNormal = x.scale(cosHalfHFov).add(z.scale(sinHalfHFov)).toNormalized();
        Cartesian3f rightNormal = x.scale(-cosHalfHFov).add(z.scale(sinHalfHFov)).toNormalized();
        Cartesian3f topNormal = y.scale(-cosHalfVFov).add(z.scale(sinHalfVFov)).toNormalized();
        Cartesian3f bottomNormal = y.scale(cosHalfVFov).add(z.scale(sinHalfVFov)).toNormalized();

        Cartesian3f nearCenter = point.add(z.scale(near));
        Cartesian3f farCenter = point.add(z.scale(far));

        this.planes = new Plane[6];
        this.planes[0] = new Plane(nearCenter, z); // Near plane
        this.planes[1] = new Plane(farCenter, z.scale(-1)); // Far plane
        this.planes[2] = new Plane(point, leftNormal); // Left plane
        this.planes[3] = new Plane(point, rightNormal); // Right plane
        this.planes[4] = new Plane(point, topNormal); // Top plane
        this.planes[5] = new Plane(point, bottomNormal); // Bottom plane
    }

    public boolean intersectsVolume(Volume volume, Matrix4f volumeTransform, SpheroidCoordinatesConverter converter) {
        return volume.intersectsPositiveSides(this.planes, volumeTransform, converter);
    }
}