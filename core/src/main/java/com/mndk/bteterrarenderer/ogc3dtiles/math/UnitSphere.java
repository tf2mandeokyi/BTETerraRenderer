package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Box;
import lombok.experimental.UtilityClass;

/**
 * The "unit sphere" described here has a center of (0, 0, 0)
 * and a radius of 1
 */
@UtilityClass
public class UnitSphere {

    private final Cartesian3 SPHERE_CENTER = Cartesian3.ORIGIN;
    private final double RADIUS = 1;

    public boolean containsCartesian(Cartesian3 cartesian) {
        return cartesian.distance2() <= RADIUS*RADIUS;
    }

    /**
     * Check if the ray intersects the unit sphere
     * @return {@code true} if intersects, {@code false} otherwise
     */
    public boolean checkRayIntersection(Cartesian3 rayStart, Cartesian3 rayEnd) {
        return checkRayIntersection(rayStart, rayEnd, Double.POSITIVE_INFINITY);
    }

    /**
     * Check if the ray intersects the unit sphere
     * @param maxInterval The maximum interval. 0 means the {@code rayStart}, 1 means the {@code rayEnd}
     * @return {@code true} if intersects, {@code false} otherwise
     */
    private boolean checkRayIntersection(Cartesian3 rayStart, Cartesian3 rayEnd, double maxInterval) {
        // Quadratic equation
        Cartesian3 velocity = rayEnd.subtract(rayStart);
        double a = velocity.dot(velocity); // D*D
        double b = velocity.dot(rayStart.scale(2)); // 2O*D
        double c = rayStart.dot(rayStart) - RADIUS*RADIUS; // O*O - 1

        double D = b*b - 4*a*c;
        if(D < 0) return false;

        double sqrtD = Math.sqrt(D);
        double[] interval = { (-b-sqrtD) / (2*a), (-b+sqrtD) / (2*a) };
        double[] intervalIntersection = UnitCube.getRangeIntersection(interval, new double[] { 0, maxInterval });
        return intervalIntersection != null;
    }

    public boolean checkEllipsoidIntersection(Matrix4 sphereMatrix) {
        // Check if the center of the unit sphere is inside the ellipsoid
        Cartesian3 transformedCenter = SPHERE_CENTER.transform(sphereMatrix.inverse().toMatrix4());
        if(containsCartesian(transformedCenter)) return true;

        Cartesian3 projected = transformedCenter.toNormalized().transform(sphereMatrix);
        return containsCartesian(projected.subtract(SPHERE_CENTER));
    }

    /**
     * This function checks if the parallelepiped intersects the unit sphere.
     * @param boxMatrix The box matrix
     * @return Whether both objects intersect each other
     */
    public boolean checkParallelepipedIntersection(Matrix4 boxMatrix) {
        Cartesian3 boxCenter = new Cartesian3(boxMatrix.get(3, 0), boxMatrix.get(3, 1), boxMatrix.get(3, 2));
        Matrix4 inverseBoxMatrix = boxMatrix.inverse().toMatrix4();

        // 1. First check if the unit sphere center is in the box
        //    This is checked by transforming the parallelepiped into a unit cube
        Cartesian3 transformedSphereCenter = SPHERE_CENTER.transform(inverseBoxMatrix);
        if(UnitCube.containsCartesian(transformedSphereCenter)) return true;

        // 2. Check all parallelepiped vertices if they're in the unit sphere
        //    The method performs visibility checks first for the performance,
        //    which is done by transforming the parallelepiped into a unit cube
        for(int[] unitVertexFeature : UnitCube.VERTEX_FEATURES) {
            if(UnitCube.isUnitFeatureHiddenToCartesian(unitVertexFeature, transformedSphereCenter)) continue;

            Cartesian3 vertex = UnitCube.unitCoordinateToCartesian(unitVertexFeature, boxMatrix);
            if(containsCartesian(vertex)) return true;
        }

        // 3. Check if the edges intersect the unit sphere
        //    This is also done after the visibility check
        for(int[] unitEdgeFeature : UnitCube.EDGE_FEATURES) {
            if(UnitCube.isUnitFeatureHiddenToCartesian(unitEdgeFeature, transformedSphereCenter)) continue;

            int[] unitRayStart = { unitEdgeFeature[0], unitEdgeFeature[1], unitEdgeFeature[2] };
            int[] unitRayEnd = { unitEdgeFeature[0], unitEdgeFeature[1], unitEdgeFeature[2] };
            for(int i = 0; i < 3; i++) {
                if(unitEdgeFeature[i] != 0) continue;
                unitRayStart[i] = 1;
                unitRayEnd[i] = -1;
            }

            Cartesian3 rayStart = UnitCube.unitCoordinateToCartesian(unitRayStart, boxMatrix);
            Cartesian3 rayEnd = UnitCube.unitCoordinateToCartesian(unitRayEnd, boxMatrix);
            if(checkRayIntersection(rayStart, rayEnd, 1)) return true;
        }

        // 4. Check if the parallelepiped side intersect the unit sphere
        //    This is also done after the visibility check
        for(int[] unitSideFeature : UnitCube.SIDE_FEATURES) {
            if(UnitCube.isUnitFeatureHiddenToCartesian(unitSideFeature, transformedSphereCenter)) continue;

            Cartesian3 planeCenter = UnitCube.unitCoordinateToCartesian(unitSideFeature, boxMatrix);
            int[] unitU0 = { 0, 0, 0 }, unitU1 = { 0, 0, 0 }, unitU2 = { 0, 0, 0 };
            for(int i = 0; i < 3; i++) {
                if(unitSideFeature[i] == 0) continue;
                unitU0[(i+1) % 3] = 1;
                unitU1[(i+2) % 3] = 1;
                unitU2[i] = 1;
            }

            Cartesian3 u0 = UnitCube.unitCoordinateToCartesian(unitU0, boxMatrix).subtract(boxCenter);
            Cartesian3 u1 = UnitCube.unitCoordinateToCartesian(unitU1, boxMatrix).subtract(boxCenter);
            Cartesian3 u2 = UnitCube.unitCoordinateToCartesian(unitU2, boxMatrix).subtract(boxCenter);
            Cartesian3 normal = u0.cross(u1).toNormalized();

            // Skip if the distance between the plane and the sphere are greater than 1
            double distance = SPHERE_CENTER.subtract(planeCenter).dot(normal);
            if(Math.abs(distance) > 1) continue;

            // Check if the projected coordinate is in the plane
            Cartesian3 projected = SPHERE_CENTER.subtract(normal.scale(distance));
            Matrix4 planeMatrix = new Box(planeCenter, Matrix3.fromCoordinates(u0, u1, u2)).boxMatrix;
            Cartesian3 t = projected.transform(planeMatrix.inverse().toMatrix4());
            if(UnitCube.containsCartesian(t)) return true;
        }

        // If none of the previous checks have succeeded, then the sphere does not intersect the parallelepiped
        return false;
    }
}
