package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.Getter;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SpheroidArc {

    private static final double[] CRITICAL_ANGLES = { 0, Math.PI / 2, Math.PI, Math.PI * 3 / 2 };

    /** In radians */
    private final double west, east, latitude;
    private final double height;

    public SpheroidArc(double west, double east, double latitude, double height) {
        this.west = normalizeAngle(west);
        this.east = normalizeAngle(east);
        this.latitude = latitude;
        this.height = height;
    }

    public AABB getBoundingBox(SpheroidCoordinatesConverter converter) {
        List<Double> angles = new ArrayList<>(4);
        for (double criticalAngle : CRITICAL_ANGLES) {
            if (this.containsAngle(criticalAngle)) {
                angles.add(criticalAngle);
            }
        }

        AABB result = AABB.fromPoint(this.toCartesian3f(west, converter))
                .include(this.toCartesian3f(east, converter));
        for (double angle : angles) {
            result = result.include(this.toCartesian3f(angle, converter));
        }
        return result;
    }

    public Vector3d toCartesian3f(double angleRadians, SpheroidCoordinatesConverter converter) {
        Spheroid3 spheroid = Spheroid3.fromRadians(angleRadians, latitude, height);
        return converter.toCartesian(spheroid);
    }

    public boolean containsAngle(double angle) {
        if (west < east) {
            return west <= angle && angle <= east;
        } else {
            return west <= angle || angle <= east;
        }
    }

    private static double normalizeAngle(double angle) {
        angle %= MathConstants.PI2;
        if (angle < 0) angle += MathConstants.PI2;
        return angle;
    }
}
