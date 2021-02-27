package com.mndk.bte_tr.map.tmap;

import com.mndk.bte_tr.map.RenderMapSource;
import com.mndk.bte_tr.map.mercator.MercatorMapManager;
import com.mndk.bte_tr.map.mercator.MercatorTileConverter;
import com.mndk.bte_tr.projection.Projections;

import copy.io.github.terra121.projection.OutOfProjectionBoundsException;

public class TMapManager extends MercatorMapManager {

    private static final int[][] CORNERS = {
            {0, 1, 0, 0}, // top left
            {1, 1, 1, 0}, // top right
            {1, 0, 1, 1}, // bottom right
            {0, 0, 0, 1}  // bottom left
    };

    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }

    public TMapManager() {
        super(RenderMapSource.TMAP, "https://topopentile2.tmap.co.kr/tms/1.0.0/hd_tile/{z}/{x}/{y}.png", 2);
    }

    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        return MercatorTileConverter.geoToTile_invertLat(temp[0], temp[1], zoom);
    }


    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = MercatorTileConverter.tileToGeo_invertLat(tileX, tileY, zoom);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }
}
