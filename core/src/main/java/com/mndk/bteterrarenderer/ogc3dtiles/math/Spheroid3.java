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

    @Override
    public String toString() {
        String latitudeString = toDegreeMinuteSecond(Math.toDegrees(latitude), true);
        String longitudeString = toDegreeMinuteSecond(Math.toDegrees(longitude), false);
        return String.format("Spheroid3[%s %s, height=%.2fm]", latitudeString, longitudeString, height);
    }

    private static String toDegreeMinuteSecond(double value, boolean isLatitude) {
        String direction = isLatitude ? (value < 0 ? "S" : "N") : (value < 0 ? "W" : "E");
        value = Math.abs(value);
        int deg = (int) value;
        value = (value - deg) * 60;
        int min = (int) value;
        value = (value - min) * 60;
        double sec = value;
        return String.format("%02d°%02d'%05.2f\"%s", deg, min, sec, direction);
    }
}
