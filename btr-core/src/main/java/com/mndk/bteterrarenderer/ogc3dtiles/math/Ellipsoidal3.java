package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import lombok.Data;

@Data
public class Ellipsoidal3 {
    /**
     * In radians
     */
    private final double longitude, latitude;
    /**
     * The height (in meters) from the surface of the sphere/ellipsoid
     */
    private final double height;

    public Cartesian3 toCartesianCoordinate() {
        double N = EllipsoidalMath.getEarthCurvatureRadius(latitude);
        double x = (N + height) * Math.cos(latitude) * Math.cos(longitude);
        double y = (N + height) * Math.cos(latitude) * Math.sin(longitude);
        double z = ((1 - Wgs84Constants.ECCENTRICITY2) * N + height) * Math.sin(latitude);
        return new Cartesian3(x, y, z);
    }
}
