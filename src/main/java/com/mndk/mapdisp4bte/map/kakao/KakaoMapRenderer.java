package com.mndk.mapdisp4bte.map.kakao;

import com.mndk.mapdisp4bte.map.ExternalMapRenderer;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import com.mndk.mapdisp4bte.projection.Projections;
import io.github.terra121.projection.OutOfProjectionBoundsException;

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
        super(RenderMapSource.KAKAO, 2);
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
    public String getUrlTemplate(int tileX, int tileY, int level, RenderMapType type) {
        String dir = type == RenderMapType.AERIAL ? "map_skyview" : "map_2d/2012tlq";
        String fileType = type == RenderMapType.AERIAL ? ".jpg" : ".png";

        return "http://map" + domain_num + ".daumcdn.net/" +
                dir + "/L" + (level + 1) + "/" + tileY + "/" + tileX + fileType;
    }


}
