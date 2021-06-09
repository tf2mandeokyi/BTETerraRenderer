package com.mndk.bteterrarenderer.tms;

import com.mndk.bteterrarenderer.projection.Proj4jProjection;
import com.mndk.bteterrarenderer.projection.Projections;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

import java.util.Map;

/**
 * EPSG:3395
 * */
public class WorldMercatorTMS extends WebMercatorTMS {


    private static final GeographicProjection EPSG3395 = new Proj4jProjection("EPSG:3395",
            "+proj=merc +lon_0=0 +k=1 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs".split(" ")
    );


    public static final double R_A = 6378137;
    public static final double EQUATOR = 2 * Math.PI * R_A;
    public static final double R_B = 6356752.3142;
    public static final double E = Math.sqrt(1 - (R_B * R_B) / (R_A * R_A));


    public static final int[] NULL_TILECOORD = { 0, 0 };
    public static final double[] NULL_LATLON = { 0, 0 };


    public WorldMercatorTMS(String id, Map<String, Object> object) throws Exception { super(id, object); }


    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        return geoToTile(temp[0], temp[1], zoom, this.invertLatitude);
    }


    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = tileToGeo(tileX, tileY, zoom, this.invertLatitude);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }


    public static int[] geoToTile(double lon, double lat, int zoom, boolean invertLatitude) {
        try {
            double n = Math.pow(2, zoom);
            double[] a = EPSG3395.fromGeo(lon, lat);
            double pixelX =  (a[0] + (EQUATOR / 2.0)) * n / EQUATOR;
            double pixelY = -(a[1] - (EQUATOR / 2.0)) * n / EQUATOR;
            if(invertLatitude) pixelY = -pixelY;
            return new int[] {
                    (int) Math.floor(pixelX),
                    (int) Math.floor(pixelY)
            };
        } catch (OutOfProjectionBoundsException e) {
            return NULL_TILECOORD;
        }
    }

    public static double[] tileToGeo(int tileX, int tileY, int zoom, boolean invertLatitude) {
        try {
            double n = Math.pow(2, zoom);
            double xCoord =  (EQUATOR * tileX / n) - EQUATOR / 2;
            double yCoord = -(EQUATOR * tileY / n) + EQUATOR / 2;
            if(invertLatitude) yCoord = -yCoord;
            return EPSG3395.toGeo(xCoord, yCoord);
        } catch (OutOfProjectionBoundsException e) {
            return NULL_LATLON;
        }
    }
}
