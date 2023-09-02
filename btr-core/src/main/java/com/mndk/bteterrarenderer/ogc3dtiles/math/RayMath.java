package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;

@UtilityClass
public class RayMath {

    /**
     * Check if the ray intersects the "unit cube", which has a center of (0, 0, 0) and
     * a halfLength of 3x3 identity matrix
     * @return {@code true} if intersects, false otherwise
     */
    public boolean checkUnitCubeIntersection(Cartesian3 rayStart, Cartesian3 rayEnd) {
        double[] xRange = getUnitRangeIntersection(rayStart.getX(), rayEnd.getX() - rayStart.getX());
        double[] yRange = getUnitRangeIntersection(rayStart.getY(), rayEnd.getY() - rayStart.getY());
        double[] zRange = getUnitRangeIntersection(rayStart.getZ(), rayEnd.getZ() - rayStart.getZ());

        double[] xyRange = getRangeIntersection(xRange, yRange);
        double[] xyzRange = getRangeIntersection(xyRange, zRange);
        return xyzRange != null;
    }

    @Nullable
    private double[] getUnitRangeIntersection(double start, double velocity) {
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
    private double[] getRangeIntersection(double[] a, double[] b) {
        if(a == null || b == null) return null;
        double min = Math.max(a[0], b[0]);
        double max = Math.min(a[1], b[1]);
        if(min > max) return null;
        return new double[] { min, max };
    }

    /**
     * Check if the ray intersects the "unit sphere", which has a center of (0, 0, 0) and
     * a radius of 1
     * @return {@code true} if intersects, false otherwise
     */
    public boolean checkUnitSphereIntersection(Cartesian3 rayStart, Cartesian3 rayEnd) {
        // Quadratic equation
        Cartesian3 direction = rayEnd.subtract(rayStart);
        double a = direction.dot(direction); // D*D
        double b = direction.dot(rayStart.scale(2)); // 2O*D
        double c = rayStart.dot(rayStart) - 1; // O*O - 1

        double discriminant = b*b - 4*a*c;
        return discriminant >= 0;
    }
}
