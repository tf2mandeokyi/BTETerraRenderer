package com.mndk.bteterrarenderer.map.kakao_wtm;

import com.google.gson.JsonObject;
import com.mndk.bteterrarenderer.map.ExternalTileMap;
import com.mndk.bteterrarenderer.projection.Projections;

import copy.io.github.terra121.projection.OutOfProjectionBoundsException;

public class KakaoTileMap extends ExternalTileMap {
	
    public KakaoTileMap(JsonObject object) throws Exception { super(object); }



    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        temp = Projections.WTM.fromGeo(temp[0], temp[1]);
        return wtmToTile(temp[0], temp[1], zoom);
    }



    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = tileToWTM(tileX, tileY, zoom);
        temp = Projections.WTM.toGeo(temp[0], temp[1]);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
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
