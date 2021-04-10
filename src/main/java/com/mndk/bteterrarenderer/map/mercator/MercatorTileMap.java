package com.mndk.bteterrarenderer.map.mercator;

import java.util.Map;

import com.mndk.bteterrarenderer.map.ExternalTileMap;
import com.mndk.bteterrarenderer.projection.Projections;

import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class MercatorTileMap extends ExternalTileMap {


	public MercatorTileMap(String id, Map<String, Object> object) throws Exception { super(id, object); }


	@Override
	public int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException {
		double[] temp = Projections.BTE.toGeo(playerX, playerZ);
		return this.invertLatitude ? 
				MercatorTileConverter.geoToTile_invertLat(temp[0], temp[1], zoom) : 
				MercatorTileConverter.geoToTile(temp[0], temp[1], zoom);
	}


	@Override
	public double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
		double[] temp = this.invertLatitude ? 
				MercatorTileConverter.tileToGeo_invertLat(tileX, tileY, zoom) :
				MercatorTileConverter.tileToGeo(tileX, tileY, zoom);
		return Projections.BTE.fromGeo(temp[0], temp[1]);
	}
}
