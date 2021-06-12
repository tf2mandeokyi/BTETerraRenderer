package com.mndk.bteterrarenderer.tms;

import java.util.Map;

/**
 * EPSG:3857 or EPSG:900913
 * */
public class WebMercatorTMS extends TileMapService {


	public WebMercatorTMS(String id, Map<String, Object> object) throws Exception { super(id, object); }


	@Override
	public int[] geoCoordToTileCoord(double longitude, double latitude, int zoom) {
		double factor = Math.pow(2, zoom);
		double a = Math.log(Math.tan(Math.toRadians(latitude)) + (1 / Math.cos(Math.toRadians(latitude)))) / Math.PI;
		if(invertLatitude) a = -a;
		return new int[] {
				(int) Math.floor(factor * (longitude + 180) / 360),
				(int) Math.floor(factor * (1 - a) / 2)
		};
	}


	@Override
	public double[] tileCoordToGeoCoord(int tileX, int tileY, int zoom) {
		double divisor = Math.pow(2, zoom);
		double lat_rad = Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * tileY) / divisor));
		if(invertLatitude) lat_rad = -lat_rad;
		return new double[] {
				(tileX * 360 / divisor) - 180,
				Math.toDegrees(lat_rad)
		};
	}

}
