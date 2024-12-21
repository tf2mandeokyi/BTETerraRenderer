package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.*;

@Setter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Spheroid3 {

    public static Spheroid3 fromDegrees(double longitude, double latitude, double height) {
        return new Spheroid3(Math.toRadians(longitude), Math.toRadians(latitude), height);
    }
    public static Spheroid3 fromRadians(double longitude, double latitude, double height) {
        return new Spheroid3(longitude, latitude, height);
    }

    /**
     * In radians
     */
	private final double longitude, latitude;
    /**
     * The height (in meters) from the surface of the sphere/spheroid
     */
    @Getter
    private final double height;

    public double getLongitudeRadians() { return longitude; }
    public double getLatitudeRadians() { return latitude; }
    public double getLongitudeDegrees() { return Math.toDegrees(longitude); }
    public double getLatitudeDegrees() { return Math.toDegrees(latitude); }

    public Spheroid3 add(Spheroid3 other) {
        double latitude = this.latitude + other.latitude;
        // clamping
        latitude = Math.abs(Math.PI - (latitude - Math.PI / 2) % MathConstants.PI2) - Math.PI / 2;
        return new Spheroid3((longitude + other.longitude) % MathConstants.PI2, latitude, height + other.height);
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
