package com.mndk.kmap4bte.map.kakao;

import com.mndk.kmap4bte.map.ExternalMapRenderer;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import io.github.terra121.projection.OutOfProjectionBoundsException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class KakaoMapRenderer extends ExternalMapRenderer {

    // Tile boundary matrix
    //
    // double[...][0: tileX_add, 1: tileY_add, 2: u, 3: v]
    private static final int[][] CORNERS = {
            {0, 1, 0, 0}, // top left
            {1, 1, 1, 0}, // top right
            {1, 0, 1, 1}, // bottom right
            {0, 0, 0, 1}  // bottom left
    };



    public KakaoMapRenderer() {
        super(RenderMapSource.KAKAO);
    }



    private static int domain_num = 0;


    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int level) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        temp = Projections.WTM.fromGeo(temp[0], temp[1]);
        return WTMTileConverter.wtmToTile(temp[0], temp[1], level + 1);
    }

    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int level) throws OutOfProjectionBoundsException {
        double[] temp = WTMTileConverter.tileToWTM(tileX, tileY, level + 1);
        temp = Projections.WTM.toGeo(temp[0], temp[1]);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }

    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }


    @Override
    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) {

        try {

            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, level);

            String dir = type == RenderMapType.AERIAL ? "map_skyview" : "map_2d/2012tlq";
            String fileType = type == RenderMapType.AERIAL ? ".jpg" : ".png";

            URL url = new URL("http://map" + domain_num + ".daumcdn.net/" +
                    dir + "/L" + (level + 1) + "/" + (tilePos[1] + tileDeltaY) + "/" + (tilePos[0] + tileDeltaX) + fileType
            );

            domain_num++;
            if(domain_num >= 4) domain_num = 0;

            return url.openConnection();

        } catch(OutOfProjectionBoundsException | IOException exception) {
            return null;
        }
    }


}
