package com.mndk.bte_tr.map.mercator;

import com.mndk.bte_tr.map.ExternalMapManager;
import com.mndk.bte_tr.map.RenderMapSource;
import com.mndk.bte_tr.projection.Projections;

import copy.io.github.terra121.projection.OutOfProjectionBoundsException;

public class MercatorMapManager extends ExternalMapManager {


    protected final String requestUrlTemplate;


    // Tile boundary matrix
    //
    // double[...][0: tileX_add, 1: tileY_add, 2: u, 3: v]
    private static final int[][] CORNERS = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };


    public MercatorMapManager(RenderMapSource source, String requestUrlTemplate, int maximumDownloadThreads) {
        super(source, maximumDownloadThreads);
        this.requestUrlTemplate = requestUrlTemplate;
    }


    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        return MercatorTileConverter.geoToTile(temp[0], temp[1], zoom);
    }


    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = MercatorTileConverter.tileToGeo(tileX, tileY, zoom);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }


    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }

    @Override
    protected int getZoomFromLevel(int level) {
        return 18 - level;
    }


    protected String getRandom() {return "";}


    @Override
    public String getUrlTemplate(int tileX, int tileY, int zoom) {
        return requestUrlTemplate.replace("{random}", this.getRandom())
                .replace("{z}", zoom + "")
                .replace("{x}", tileX + "")
                .replace("{y}", tileY + "");
    }
}
