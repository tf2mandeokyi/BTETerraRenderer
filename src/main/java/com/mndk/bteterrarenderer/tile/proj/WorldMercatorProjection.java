package com.mndk.bteterrarenderer.tile.proj;

import com.mndk.bteterrarenderer.projection.Proj4jProjection;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class WorldMercatorProjection extends TileProjection {


    private static final GeographicProjection EPSG3395 = new Proj4jProjection("EPSG:3395",
            "+proj=merc +lon_0=0 +k=1 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs".split(" ")
    );
    public static final double R_A = 6378137;
    public static final double EQUATOR = 2 * Math.PI * R_A;


    public WorldMercatorProjection() {
        super();
    }


    @Override
    public int[] toTileCoord(double longitude, double latitude, int absoluteZoom) throws OutOfProjectionBoundsException {
        double n = Math.pow(2, absoluteZoom);
        double[] a = EPSG3395.fromGeo(longitude, latitude);
        double pixelX =  (a[0] + (EQUATOR / 2.0)) * n / EQUATOR;
        double pixelY = -(a[1] - (EQUATOR / 2.0)) * n / EQUATOR;
        if(invertLatitude) pixelY = -pixelY;
        return new int[] {
                (int) Math.floor(pixelX),
                (int) Math.floor(pixelY)
        };
    }


    @Override
    public double[] toGeoCoord(int tileX, int tileY, int absoluteZoom) throws OutOfProjectionBoundsException {
        double n = Math.pow(2, absoluteZoom);
        double xCoord =  (EQUATOR * tileX / n) - EQUATOR / 2;
        double yCoord = -(EQUATOR * tileY / n) + EQUATOR / 2;
        if(invertLatitude) yCoord = -yCoord;
        return EPSG3395.toGeo(xCoord, yCoord);
    }
}
