package com.mndk.mapdisp4bte.map.kakao;

public class WTMTileConverter {

    public static double[] tileToWTM(double tileX, double tileY, int level) {
        double factor = Math.pow(2, level - 3) * 256;
        return new double[] {tileX * factor - 30000, tileY * factor - 60000};
    }

    public static int[] wtmToTile(double wtmX, double wtmY, int level) {
        double divisor = Math.pow(2, level - 3) * 256;
        return new int[] {(int) Math.floor((wtmX + 30000) / divisor), (int) Math.floor((wtmY + 60000) / divisor)};
    }
}
