package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

/**
 * The "unit cube" described here has a center of (0, 0, 0) and
 * a halfLength of 3x3 identity matrix
 */
@UtilityClass
public class UnitCube {

    /**
     * This is package-private, as it will be used in {@link UnitSphere}
     */
    public final int[][] VERTEX_FEATURES = {
            { +1, +1, +1 },
            { +1, +1, -1 },
            { +1, -1, +1 },
            { +1, -1, -1 },
            { -1, +1, +1 },
            { -1, +1, -1 },
            { -1, -1, +1 },
            { -1, -1, -1 }
    };
    /**
     * This is package-private, as it will be used in {@link UnitSphere}
     */
    public final int[][] EDGE_FEATURES = {
            // cube top
            { +1,  0,  1 },
            { -1,  0,  1 },
            {  0, +1,  1 },
            {  0, -1,  1 },
            // cube sides
            { +1, +1,  0 },
            { +1, -1,  0 },
            { -1, -1,  0 },
            { -1, +1,  0 },
            // cube bottom
            { +1,  0, -1 },
            { -1,  0, -1 },
            {  0, +1, -1 },
            {  0, -1, -1 }
    };
    /**
     * This is package-private, as it will be used in {@link UnitSphere}
     */
    public final int[][] SIDE_FEATURES = {
            { +1,  0,  0 },
            { -1,  0,  0 },
            {  0, +1,  0 },
            {  0, -1,  0 },
            {  0,  0, +1 },
            {  0,  0, -1 },
    };

    /**
     * This method checks if the cartesian coordinate can "see" the "unit feature",
     * which could be either a vertex, an edge, or a cube side
     * @param unitCubeFeature A "feature" of a unit cube
     * @param cartesian The coordinate
     * @return The result
     */
    public boolean isUnitFeatureHiddenToCartesian(int[] unitCubeFeature, Cartesian3f cartesian) {
        double[] temp = { cartesian.getX(), cartesian.getY(), cartesian.getZ() };
        for(int i = 0; i < 3; i++) {
            int unitAxis = unitCubeFeature[i];
            double coord = temp[i];
            if(unitAxis == 0) continue;

            if(unitAxis * coord >= 0 && Math.abs(coord) >= Math.abs(unitAxis)) return false;
        }
        return true;
    }

    public Cartesian3f unitCoordinateToCartesian(int[] unitCubeFeature, Matrix4f boxMatrix) {
        Cartesian3f unitCoordinate = new Cartesian3f(unitCubeFeature[0], unitCubeFeature[1], unitCubeFeature[2]);
        return unitCoordinate.transform(boxMatrix);
    }

    public boolean containsCartesian(Cartesian3f cartesian) {
        double x = cartesian.getX();
        double y = cartesian.getY();
        double z = cartesian.getZ();
        return -1 <= x && x <= 1 &&
                -1 <= y && y <= 1 &&
                -1 <= z && z <= 1;
    }

    /**
     * Check if the ray intersects the unit cube, which
     * @return {@code true} if intersects, false otherwise
     */
    public boolean checkRayIntersection(Cartesian3f rayStart, Cartesian3f rayEnd) {
        double[] xRange = get1dRayIntersection(rayStart.getX(), rayEnd.getX() - rayStart.getX());
        double[] yRange = get1dRayIntersection(rayStart.getY(), rayEnd.getY() - rayStart.getY());
        double[] zRange = get1dRayIntersection(rayStart.getZ(), rayEnd.getZ() - rayStart.getZ());

        double[] xyRange = getRangeIntersection(xRange, yRange);
        double[] xyzRange = getRangeIntersection(xyRange, zRange);
        return xyzRange != null;
    }

    @Nullable
    private double[] get1dRayIntersection(double start, double velocity) {
        if(velocity == 0) {
            if(start >= -1 && start <= 1) return new double[] { 0, Double.POSITIVE_INFINITY };
            return null;
        }

        double left = (-1-start) / velocity, right = (1-start) / velocity;
        double[] result = null;
        if(velocity > 0) {
            if(right >= 0) result = new double[] { Math.max(0, left), right };
        } else {
            if(left >= 0) result = new double[] { Math.max(0, right), left };
        }
        return result;
    }

    @Nullable
    public double[] getRangeIntersection(double[] a, double[] b) {
        if(a == null || b == null) return null;
        double min = Math.max(a[0], b[0]);
        double max = Math.min(a[1], b[1]);
        if(min > max) return null;
        return new double[] { min, max };
    }

    public boolean rangeIntersects(double a1, double a2, double b1, double b2) {
        double min = Math.max(a1, b1);
        double max = Math.min(a2, b2);
        return min <= max;
    }
}
