package com.mndk.mapdisp4bte.map.kakao;

import com.mndk.mapdisp4bte.map.ExternalMapManager;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import com.mndk.mapdisp4bte.projection.Projections;
import net.buildtheearth.terraplusplus.projection.OutOfProjectionBoundsException;

public class KakaoMapManager extends ExternalMapManager {

    private static final int[][] CORNERS = {
            {0, 1, 0, 0}, // top left
            {1, 1, 1, 0}, // top right
            {1, 0, 1, 1}, // bottom right
            {0, 0, 0, 1}  // bottom left
    };



    public KakaoMapManager() {
        super(RenderMapSource.KAKAO, 2);
    }



    private static int domain_num = 0;



    @Override
    public int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = Projections.BTE.toGeo(playerX, playerZ);
        temp = Projections.WTM.fromGeo(temp[0], temp[1]);
        return WTMTileConverter.wtmToTile(temp[0], temp[1], zoom);
    }



    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException {
        double[] temp = WTMTileConverter.tileToWTM(tileX, tileY, zoom);
        temp = Projections.WTM.toGeo(temp[0], temp[1]);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }



    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }

    @Override
    protected int getZoomFromLevel(int level) {
        return level + 1;
    }


    @Override
    public String getUrlTemplate(int tileX, int tileY, int zoom, RenderMapType type) {
        String dir = type == RenderMapType.AERIAL ? "map_skyview" : "map_2d/2012tlq";
        String fileType = type == RenderMapType.AERIAL ? ".jpg" : ".png";

        if(domain_num >= 3) domain_num = -1;
        domain_num++;

        return "http://map" + domain_num + ".daumcdn.net/" +
                dir + "/L" + zoom + "/" + tileY + "/" + tileX + fileType;
    }


}
