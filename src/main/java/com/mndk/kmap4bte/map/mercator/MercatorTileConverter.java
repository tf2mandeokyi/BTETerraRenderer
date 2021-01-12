package com.mndk.kmap4bte.map.mercator;

public class MercatorTileConverter {

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

    public static String tileXYToQuadKey(int tileX, int tileY, int levelOfDetail) {
        StringBuilder quadKey = new StringBuilder();
        for (int i = levelOfDetail; i > 0; i--) {
            char digit = '0';
            int mask = 1 << (i - 1);
            if ((tileX & mask) != 0) digit++;
            if ((tileY & mask) != 0) digit+=2;
            quadKey.append(digit);
        }
        return quadKey.toString();
    }

    public static void main(String[] args) {
        int[] a = geoToTile(126.994453, 37.433197, 18);
        System.out.println(a[0] + ", " + a[1]);
    }

}
