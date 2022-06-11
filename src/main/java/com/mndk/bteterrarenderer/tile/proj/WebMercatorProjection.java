package com.mndk.bteterrarenderer.tile.proj;

public class WebMercatorProjection extends TileProjection {

    public WebMercatorProjection() {
        super();
    }

    @Override
    public int[] toTileCoord(double longitude, double latitude, int absoluteZoom) {
        double factor = Math.pow(2, absoluteZoom);
        double a = Math.log(Math.tan(Math.toRadians(latitude)) + (1 / Math.cos(Math.toRadians(latitude)))) / Math.PI;
        if(invertLatitude) a = -a;
        return new int[] {
                (int) Math.floor(factor * (longitude + 180) / 360),
                (int) Math.floor(factor * (1 - a) / 2)
        };
    }

    @Override
    public double[] toGeoCoord(int tileX, int tileY, int absoluteZoom) {
        double divisor = Math.pow(2, absoluteZoom);
        double lat_rad = Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * tileY) / divisor));
        if(invertLatitude) lat_rad = -lat_rad;
        return new double[] {
                (tileX * 360 / divisor) - 180,
                Math.toDegrees(lat_rad)
        };
    }
}
