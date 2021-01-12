package com.mndk.kmap4bte.map.osm;

public class OsmTileConverter {

    private static final double RADIAN = Math.PI / 180;
    private static final double DEGREE = 180 / Math.PI;

    public static int[] geoToTile(double lon, double lat, int level) {
        double factor = Math.pow(2, level);
        return new int[] {
                (int) Math.floor(factor * ((lon + 180) / 360)),
                (int) Math.floor(factor * (1 - (Math.log(Math.tan(Math.toRadians(lat)) + (1 / Math.cos(Math.toRadians(lat)))) / Math.PI)) / 2)
        };
    }

    public static double[] tileToGeo(int tileX, int tileY, int level) {
        double divisor = Math.pow(2, level);
        double n = Math.PI - (2.0 * Math.PI * tileY) / Math.pow(2.0, level);
        return new double[] {
                (tileX * 360 / divisor) - 180,
                Math.toDegrees(Math.atan(Math.sinh(n)))
        };
    }

}
