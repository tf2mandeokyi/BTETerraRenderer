package com.mndk.bteterrarenderer.tms.proj;

import com.mndk.bteterrarenderer.projection.Proj4jProjection;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class KakaoTileProjection extends TileProjection {


    private static final GeographicProjection EPSG5186 = new Proj4jProjection("EPSG:5186", new String[] {
            "+proj=tmerc", "+lat_0=38", "+lon_0=127", "+k=1", "+x_0=200000", "+y_0=500000", "+ellps=GRS80", "+units=m", "+no_defs"
    });


    public KakaoTileProjection() {
        super();
    }


    @Override
    public int[] toTileCoord(double longitude, double latitude, int absoluteZoom) throws OutOfProjectionBoundsException {
        double[] temp = EPSG5186.fromGeo(longitude, latitude);
        return wtmToTile(temp[0], temp[1], absoluteZoom);
    }

    @Override
    public double[] toGeoCoord(int tileX, int tileY, int absoluteZoom) throws OutOfProjectionBoundsException {
        double[] temp = tileToWTM(tileX, tileY, absoluteZoom);
        return EPSG5186.toGeo(temp[0], temp[1]);
    }


    public static double[] tileToWTM(double tileX, double tileY, int level) {
        double factor = Math.pow(2, level - 3) * 256;
        return new double[] {tileX * factor - 30000, tileY * factor - 60000};
    }

    public static int[] wtmToTile(double wtmX, double wtmY, int level) {
        double divisor = Math.pow(2, level - 3) * 256;
        return new int[] {(int) Math.floor((wtmX + 30000) / divisor), (int) Math.floor((wtmY + 60000) / divisor)};
    }

}
