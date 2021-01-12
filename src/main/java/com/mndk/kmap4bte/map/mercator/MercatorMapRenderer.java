package com.mndk.kmap4bte.map.mercator;

import com.mndk.kmap4bte.map.ExternalMapRenderer;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import io.github.terra121.projection.OutOfProjectionBoundsException;

public abstract class MercatorMapRenderer extends ExternalMapRenderer {


    private final String plainMapTemplate, aerialTemplate;


    private static final int[][] CORNERS = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };


    public MercatorMapRenderer(RenderMapSource source, String plainMapTemplate, String aerialTemplate) {
        super(source);
        this.plainMapTemplate = plainMapTemplate;
        this.aerialTemplate = aerialTemplate;
    }


    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int level) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        return MercatorTileConverter.geoToTile(temp[0], temp[1], 18 - level);
    }


    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int level) throws OutOfProjectionBoundsException {
        double[] temp = MercatorTileConverter.tileToGeo(tileX, tileY, 18 - level);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }


    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }


    protected String getRandom() {return "";}


    @Override
    public String getUrlTemplate(int tileX, int tileY, int level, RenderMapType type) {
        String template = type == RenderMapType.AERIAL ? aerialTemplate : plainMapTemplate;

        return template.replace("{random}", this.getRandom())
                .replace("{z}", (18 - level) + "")
                .replace("{x}", tileX + "")
                .replace("{y}", tileY + "");
    }
}
