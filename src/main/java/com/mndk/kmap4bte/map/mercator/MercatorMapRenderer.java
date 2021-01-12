package com.mndk.kmap4bte.map.mercator;

import com.mndk.kmap4bte.map.ExternalMapRenderer;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import io.github.terra121.projection.OutOfProjectionBoundsException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public abstract class MercatorMapRenderer extends ExternalMapRenderer {


    private final String urlTemplate;


    private static final int[][] CORNERS = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };


    public MercatorMapRenderer(RenderMapSource source, String urlTemplate) {
        super(source);
        this.urlTemplate = urlTemplate;
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


    public String getUrlTemplate(int tileX, int tileY, int level) {
        return urlTemplate
                .replace("{z}", (18 - level) + "")
                .replace("{x}", tileX + "")
                .replace("{y}", tileY + "");
    }


    @Override
    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) {
        try {
            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, level);

            String url = this.getUrlTemplate(tilePos[0] + tileDeltaX, tilePos[1] + tileDeltaY, level);

            System.out.println(url);

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

            return connection;
        }catch(OutOfProjectionBoundsException | IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
