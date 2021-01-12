package com.mndk.kmap4bte.map.osm;

import com.mndk.kmap4bte.ModReference;
import com.mndk.kmap4bte.map.CustomMapRenderer;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import io.github.terra121.projection.OutOfProjectionBoundsException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class OpenStreetMapRenderer extends CustomMapRenderer {

    private static final int[][] CORNERS = {
            {0, 1, 0, 1}, // top left
            {1, 1, 1, 1}, // top right
            {1, 0, 1, 0}, // bottom right
            {0, 0, 0, 0}  // bottom left
    };

    public OpenStreetMapRenderer() {
        super(RenderMapSource.OSM);
    }

    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int level) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        return OsmTileConverter.geoToTile(temp[0], temp[1], 18 - level);
    }

    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int level) throws OutOfProjectionBoundsException {
        double[] temp = OsmTileConverter.tileToGeo(tileX, tileY, 18 - level);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }

    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }

    private static int domain_num = 0;
    private static final char[] asdf = {'a', 'b', 'c'};

    @Override
    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) {

        try {
            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, level);

            URL url = new URL("https://" + asdf[domain_num] + ".tile.openstreetmap.org/" +
                    (18 - level) + "/" +
                    (tilePos[0] + tileDeltaX) + "/" +
                    (tilePos[1] + tileDeltaY) + ".png");

            domain_num++;
            if(domain_num >= 3) domain_num = 0;

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", ModReference.MODID + "/" + ModReference.VERSION + " Java/1.8");
            connection.setUseCaches(true);

            return connection;

        } catch(OutOfProjectionBoundsException | IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
