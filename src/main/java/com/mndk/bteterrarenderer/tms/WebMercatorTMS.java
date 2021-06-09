package com.mndk.bteterrarenderer.tms;

import com.mndk.bteterrarenderer.projection.Projections;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

import java.util.Map;

/**
 * EPSG:3857 or EPSG:900913
 * */
public class WebMercatorTMS extends TileMapService {


	public WebMercatorTMS(String id, Map<String, Object> object) throws Exception { super(id, object); }


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


	/**
	 * Converts geo coordinate into tile coordinate
	 * @param lon longitude
	 * @param lat latitude
	 * @param zoom zoom value (often known as "z value")
	 * @param invertLatitude whether to invert the latitude
	 * @return tile coordinate
	 */
	public static int[] geoToTile(double lon, double lat, int zoom, boolean invertLatitude) {
		double factor = Math.pow(2, zoom);
		double a = Math.log(Math.tan(Math.toRadians(lat)) + (1 / Math.cos(Math.toRadians(lat)))) / Math.PI;
		if(invertLatitude) a = -a;
		return new int[] {
				(int) Math.floor(factor * (lon + 180) / 360),
				(int) Math.floor(factor * (1 - a) / 2)
		};
	}


	/**
	 * Converts tile coordinate into geo coordinate
	 * @param tileX tile's x coordinate
	 * @param tileY tile's y coordinate
	 * @param zoom zoom value (often known as "z value")
	 * @param invertLatitude whether to invert the latitude
	 * @return geo coordinate
	 */
	public static double[] tileToGeo(int tileX, int tileY, int zoom, boolean invertLatitude) {
		double divisor = Math.pow(2, zoom);
		double lat_rad = Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * tileY) / divisor));
		if(invertLatitude) lat_rad = -lat_rad;
		return new double[] {
				(tileX * 360 / divisor) - 180,
				Math.toDegrees(lat_rad)
		};
	}

}
