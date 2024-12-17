package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Box;
import lombok.experimental.UtilityClass;

/**
 * The "unit sphere" described here has a center of (0, 0, 0)
 * and a radius of 1
 */
@UtilityClass
public class UnitSphere {

    private final Cartesian3f SPHERE_CENTER = Cartesian3f.ORIGIN;
    private final double RADIUS = 1;

    public boolean containsCartesian(Cartesian3f cartesian) {
        return cartesian.distance2() <= RADIUS*RADIUS;
    }

    /**
     * Check if the ray intersects the unit sphere
     * @return {@code true} if intersects, {@code false} otherwise
     */
    public boolean checkRayIntersection(Cartesian3f rayStart, Cartesian3f rayEnd) {
        return checkRayIntersection(rayStart, rayEnd, Double.POSITIVE_INFINITY);
    }

    /**
     * Check if the ray intersects the unit sphere
     * @param maxInterval The maximum interval. 0 means the {@code rayStart}, 1 means the {@code rayEnd}
     * @return {@code true} if intersects, {@code false} otherwise
     */
    private boolean checkRayIntersection(Cartesian3f rayStart, Cartesian3f rayEnd, double maxInterval) {
        // Quadratic equation
        Cartesian3f velocity = rayEnd.subtract(rayStart);
        double a = velocity.dot(velocity); // D*D
        double b = velocity.dot(rayStart.scale(2)); // 2O*D
        double c = rayStart.dot(rayStart) - RADIUS*RADIUS; // O*O - 1

        double D = b*b - 4*a*c;
        if (D < 0) return false;

        double sqrtD = Math.sqrt(D);
        double[] interval = { (-b-sqrtD) / (2*a), (-b+sqrtD) / (2*a) };
        double[] intervalIntersection = UnitCube.getRangeIntersection(interval, new double[] { 0, maxInterval });
        return intervalIntersection != null;
    }

    public boolean checkEllipsoidIntersection(Matrix4f sphereMatrix) {
        // Check if the center of the unit sphere is inside the ellipsoid
        Matrix4f inverseSphereMatrix = sphereMatrix.inverse();
        if (inverseSphereMatrix == null) return false;
        Cartesian3f transformedCenter = SPHERE_CENTER.transform(inverseSphereMatrix);
        if (containsCartesian(transformedCenter)) return true;

        Cartesian3f projected = transformedCenter.toNormalized().transform(sphereMatrix);
        return containsCartesian(projected.subtract(SPHERE_CENTER));
    }

    /**
     * This function checks if the parallelepiped intersects the unit sphere.
     * @param boxMatrix The box matrix
     * @return Whether both objects intersect each other
     */
    public boolean checkParallelepipedIntersection(Matrix4f boxMatrix) {
        Cartesian3f boxCenter = new Cartesian3f(boxMatrix.get(3, 0), boxMatrix.get(3, 1), boxMatrix.get(3, 2));
        Matrix4f inverseBoxMatrix = boxMatrix.inverse();
        if (inverseBoxMatrix == null) return false;

        // 1. First check if the unit sphere center is in the box
        //    This is checked by transforming the parallelepiped into a unit cube
        Cartesian3f transformedSphereCenter = SPHERE_CENTER.transform(inverseBoxMatrix);
        if (UnitCube.containsCartesian(transformedSphereCenter)) return true;

        // 2. Check all parallelepiped vertices if they're in the unit sphere
        //    The method performs visibility checks first for the performance,
        //    which is done by transforming the parallelepiped into a unit cube
        for (int[] unitVertexFeature : UnitCube.VERTEX_FEATURES) {
            if (UnitCube.isUnitFeatureHiddenToCartesian(unitVertexFeature, transformedSphereCenter)) continue;

            Cartesian3f vertex = UnitCube.unitCoordinateToCartesian(unitVertexFeature, boxMatrix);
            if (containsCartesian(vertex)) return true;
        }

        // 3. Check if the edges intersect the unit sphere
        //    This is also done after the visibility check
        for (int[] unitEdgeFeature : UnitCube.EDGE_FEATURES) {
            if (UnitCube.isUnitFeatureHiddenToCartesian(unitEdgeFeature, transformedSphereCenter)) continue;

            int[] unitRayStart = { unitEdgeFeature[0], unitEdgeFeature[1], unitEdgeFeature[2] };
            int[] unitRayEnd = { unitEdgeFeature[0], unitEdgeFeature[1], unitEdgeFeature[2] };
            for (int i = 0; i < 3; i++) {
                if (unitEdgeFeature[i] != 0) continue;
                unitRayStart[i] = 1;
                unitRayEnd[i] = -1;
            }

            Cartesian3f rayStart = UnitCube.unitCoordinateToCartesian(unitRayStart, boxMatrix);
            Cartesian3f rayEnd = UnitCube.unitCoordinateToCartesian(unitRayEnd, boxMatrix);
            if (checkRayIntersection(rayStart, rayEnd, 1)) return true;
        }

        // 4. Check if the parallelepiped side intersect the unit sphere
        //    This is also done after the visibility check
        for (int[] unitSideFeature : UnitCube.SIDE_FEATURES) {
            if (UnitCube.isUnitFeatureHiddenToCartesian(unitSideFeature, transformedSphereCenter)) continue;

            Cartesian3f planeCenter = UnitCube.unitCoordinateToCartesian(unitSideFeature, boxMatrix);
            int[] unitU0 = { 0, 0, 0 }, unitU1 = { 0, 0, 0 }, unitU2 = { 0, 0, 0 };
            for (int i = 0; i < 3; i++) {
                if (unitSideFeature[i] == 0) continue;
                unitU0[(i+1) % 3] = 1;
                unitU1[(i+2) % 3] = 1;
                unitU2[i] = 1;
            }

            Cartesian3f u0 = UnitCube.unitCoordinateToCartesian(unitU0, boxMatrix).subtract(boxCenter);
            Cartesian3f u1 = UnitCube.unitCoordinateToCartesian(unitU1, boxMatrix).subtract(boxCenter);
            Cartesian3f u2 = UnitCube.unitCoordinateToCartesian(unitU2, boxMatrix).subtract(boxCenter);
            Cartesian3f normal = u0.cross(u1).toNormalized();

            // Skip if the distance between the plane and the sphere are greater than 1
            float distance = SPHERE_CENTER.subtract(planeCenter).dot(normal);
            if (Math.abs(distance) > 1) continue;

            // Check if the projected coordinate is in the plane
            Cartesian3f projected = SPHERE_CENTER.subtract(normal.scale(distance));
            Matrix4f planeMatrix = new Box(planeCenter, Matrix3f.fromCoordinates(u0, u1, u2)).boxMatrix;
            Matrix4f inversePlaneMatrix = planeMatrix.inverse();
            if (inversePlaneMatrix == null) continue;
            Cartesian3f t = projected.transform(inversePlaneMatrix);
            if (UnitCube.containsCartesian(t)) return true;
        }

        // If none of the previous checks have succeeded, then the sphere does not intersect the parallelepiped
        return false;
    }
}
