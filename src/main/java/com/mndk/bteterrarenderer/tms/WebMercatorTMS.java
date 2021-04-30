package com.mndk.bteterrarenderer.tms;

import java.util.Map;

import com.mndk.bteterrarenderer.tms.TileMapService;
import com.mndk.bteterrarenderer.projection.Projections;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class WebMercatorTMS extends TileMapService {


	public WebMercatorTMS(String id, Map<String, Object> object) throws Exception { super(id, object); }


	@Override
	public int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException {
		double[] temp = Projections.BTE.toGeo(playerX, playerZ);
		return this.invertLatitude ?
				geoToTile_invertLat(temp[0], temp[1], zoom) :
				geoToTile(temp[0], temp[1], zoom);
	}


	@Override
	public double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
		double[] temp = this.invertLatitude ? 
				tileToGeo_invertLat(tileX, tileY, zoom) :
				tileToGeo(tileX, tileY, zoom);
		return Projections.BTE.fromGeo(temp[0], temp[1]);
	}


	public static int[] geoToTile(double lon, double lat, int zoom) {
		double factor = Math.pow(2, zoom);
		return new int[] {
				(int) Math.floor(factor * ((lon + 180) / 360)),
				(int) Math.floor(factor * (1 - (Math.log(Math.tan(Math.toRadians(lat)) + (1 / Math.cos(Math.toRadians(lat)))) / Math.PI)) / 2)
		};
	}


	public static int[] geoToTile_invertLat(double lon, double lat, int zoom) {
		double factor = Math.pow(2, zoom);
		return new int[] {
				(int) Math.floor(factor * ((lon + 180) / 360)),
				(int) Math.floor(factor * (1 + (Math.log(Math.tan(Math.toRadians(lat)) + (1 / Math.cos(Math.toRadians(lat)))) / Math.PI)) / 2)
		};
	}


	public static double[] tileToGeo(int tileX, int tileY, int zoom) {
		double divisor = Math.pow(2, zoom);
		double n = Math.PI - (2.0 * Math.PI * tileY) / Math.pow(2.0, zoom);
		return new double[] {
				(tileX * 360 / divisor) - 180,
				Math.toDegrees(Math.atan(Math.sinh(n)))
		};
	}


	public static double[] tileToGeo_invertLat(int tileX, int tileY, int zoom) {
		double divisor = Math.pow(2, zoom);
		double n = - Math.PI + (2.0 * Math.PI * tileY) / Math.pow(2.0, zoom);
		return new double[] {
				(tileX * 360 / divisor) - 180,
				Math.toDegrees(Math.atan(Math.sinh(n)))
		};
	}

}
