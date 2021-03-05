package com.mndk.bte_tr.map_new.mercator;

import com.google.gson.JsonObject;
import com.mndk.bte_tr.map_new.NewExternalMapManager;
import com.mndk.bte_tr.projection.Projections;

import copy.io.github.terra121.projection.OutOfProjectionBoundsException;

public class NewMercatorMapManager extends NewExternalMapManager {


    public NewMercatorMapManager(JsonObject object) throws Exception { super(object); }


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
