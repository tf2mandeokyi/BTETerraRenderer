package com.mndk.bteterrarenderer.ogc3dtiles;

/** Based on WGS84 */
public class Wgs84Constants {

    /** In meters */
    public static final double SEMI_MAJOR_AXIS = 6_378_137.0;
    /** In meters */
    public static final double SEMI_MINOR_AXIS = 6_356_752.314245;
    /** The Earth's eccentricity squared, which is calculated by {@code (a^2 - b^2) / a^2} */
    public static final double ECCENTRICITY2 =
            (Math.pow(SEMI_MAJOR_AXIS, 2) - Math.pow(SEMI_MINOR_AXIS, 2)) / Math.pow(SEMI_MAJOR_AXIS, 2);

}
