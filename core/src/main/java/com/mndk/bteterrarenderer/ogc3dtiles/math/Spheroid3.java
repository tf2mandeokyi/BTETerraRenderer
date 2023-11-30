package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import lombok.Data;

@Data
public class Spheroid3 {
    /**
     * In radians
     */
	private final double longitude, latitude;
    /**
     * The height (in meters) from the surface of the sphere/spheroid
     */
    private final double height;

    public Cartesian3 toCartesianCoordinate() {
        double N = SpheroidalMath.getEarthCurvatureRadius(latitude);
        double x = (N + height) * Math.cos(latitude) * Math.cos(longitude);
        double y = (N + height) * Math.cos(latitude) * Math.sin(longitude);
        double z = ((1 - Wgs84Constants.ECCENTRICITY2) * N + height) * Math.sin(latitude);
        return new Cartesian3(x, y, z);
    }

    public double getLongitudeDegrees() { return Math.toDegrees(longitude); }
    public double getLatitudeDegrees() { return Math.toDegrees(latitude); }

    public Spheroid3 add(Spheroid3 other) {
        double latitude = this.latitude + other.latitude;
        // clamping
        latitude = Math.abs(Math.PI - (latitude - Math.PI / 2) % (2 * Math.PI)) - Math.PI / 2;
        return new Spheroid3((longitude + other.longitude) % (2 * Math.PI), latitude, height + other.height);
    }
}
