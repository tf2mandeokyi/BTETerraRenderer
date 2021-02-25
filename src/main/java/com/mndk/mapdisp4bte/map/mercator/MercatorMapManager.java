package com.mndk.mapdisp4bte.map.mercator;

import com.mndk.mapdisp4bte.map.ExternalMapManager;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import com.mndk.mapdisp4bte.projection.Projections;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class MercatorMapManager extends ExternalMapManager {


    private final String plainMapTemplate, aerialTemplate;


    // Tile boundary matrix
    //
    // double[...][0: tileX_add, 1: tileY_add, 2: u, 3: v]
    private static final int[][] CORNERS = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };


    public MercatorMapManager(RenderMapSource source, String plainMapTemplate, String aerialTemplate, int maximumDownloadThreads) {
        super(source, maximumDownloadThreads);
        this.plainMapTemplate = plainMapTemplate;
        this.aerialTemplate = aerialTemplate;
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
    public String getUrlTemplate(int tileX, int tileY, int zoom, RenderMapType type) {
        String template = type == RenderMapType.AERIAL ? aerialTemplate : plainMapTemplate;

        return template.replace("{random}", this.getRandom())
                .replace("{z}", zoom + "")
                .replace("{x}", tileX + "")
                .replace("{y}", tileY + "");
    }
}
