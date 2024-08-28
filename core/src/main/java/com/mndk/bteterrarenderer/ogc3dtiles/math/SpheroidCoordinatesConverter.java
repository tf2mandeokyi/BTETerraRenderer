package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import lombok.Getter;

@Getter
public class SpheroidCoordinatesConverter {

    public static final SpheroidCoordinatesConverter WGS84 =
        new SpheroidCoordinatesConverter(Wgs84Constants.SEMI_MAJOR_AXIS, Wgs84Constants.SEMI_MINOR_AXIS);

    private static final int LATITUDE_APPROX_ITERATION = 5;

    private final double semiMajorAxis;
    private final double semiMinorAxis;
    private final double eccentricitySquared;

    public SpheroidCoordinatesConverter(double semiMajorAxis, double semiMinorAxis) {
        this.semiMajorAxis = semiMajorAxis;
        this.semiMinorAxis = semiMinorAxis;
        this.eccentricitySquared = getEccentricitySquared(semiMajorAxis, semiMinorAxis);
    }

    /**
     * @link <a href="https://gssc.esa.int/navipedia/index.php/Ellipsoidal_and_Cartesian_Coordinates_Conversion">
     *     Ellipsoidal and Cartesian Coordinates Conversion</a>
     * @param latitude The latitude, in radians
     * @return The radius of curvature in the prime vertical
     */
    public double getCurvatureRadius(double latitude) {
        return semiMajorAxis / Math.sqrt(1 - (eccentricitySquared * Math.pow(Math.sin(latitude), 2)));
    }

    public Cartesian3 toCartesian(Spheroid3 spheroid) {
        double R = this.getCurvatureRadius(spheroid.getLatitude());
        double x = (R + spheroid.getHeight()) * Math.cos(spheroid.getLatitude()) * Math.cos(spheroid.getLongitude());
        double y = (R + spheroid.getHeight()) * Math.cos(spheroid.getLatitude()) * Math.sin(spheroid.getLongitude());
        double z = ((1 - this.eccentricitySquared) * R + spheroid.getHeight()) * Math.sin(spheroid.getLatitude());
        return new Cartesian3(x, y, z);
    }

    /**
     * @link <a href="https://gssc.esa.int/navipedia/index.php/Ellipsoidal_and_Cartesian_Coordinates_Conversion">
     *     Ellipsoidal and Cartesian Coordinates Conversion</a>
     * @return The spheroidal coordinate corresponding to the given cartesian coordinate
     */
    public Spheroid3 toSpheroid(Cartesian3 cartesian) {
        double x = cartesian.getX();
        double y = cartesian.getY();
        double z = cartesian.getZ();

        double longitude = Math.atan2(y, x);
        double p = Math.sqrt(x*x + y*y);
        double latitude = Math.atan2(z, (1 - this.eccentricitySquared) * p);
        double height = 0;
        for (int i = 0; i < LATITUDE_APPROX_ITERATION; i++) {
            double N = this.getCurvatureRadius(latitude);
            height = p / Math.cos(latitude) - N;
            latitude = Math.atan2(z, (1 - this.eccentricitySquared * N / (N + height)) * p);
        }

        return Spheroid3.fromRadians(longitude, latitude, height);
    }

    public static double getEccentricitySquared(double semiMajorAxis, double semiMinorAxis) {
        return (Math.pow(semiMajorAxis, 2) - Math.pow(semiMinorAxis, 2)) / Math.pow(semiMajorAxis, 2);
    }

}
