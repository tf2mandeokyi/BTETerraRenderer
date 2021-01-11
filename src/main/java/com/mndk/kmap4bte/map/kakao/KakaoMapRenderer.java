package com.mndk.kmap4bte.map.kakao;

import com.mndk.kmap4bte.map.CustomMapRenderer;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import com.mndk.kmap4bte.projection.wtm.WTMTileConverter;
import io.github.terra121.projection.OutOfProjectionBoundsException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class KakaoMapRenderer extends CustomMapRenderer {

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
        return WTMTileConverter.wtmToTile(temp[0], temp[1], level);
    }

    @Override
    public double[] tileCoordToPlayerPosition(int tileX, int tileY, int level) throws OutOfProjectionBoundsException {
        double[] temp = WTMTileConverter.tileToWTM(tileX, tileY, level);
        temp = Projections.WTM.toGeo(temp[0], temp[1]);
        return Projections.BTE.fromGeo(temp[0], temp[1]);
    }

    @Override
    protected int[] getCornerMatrix(int i) {
        return CORNERS[i];
    }


    @Override
    public BufferedImage fetchMap(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) throws IOException {

        try {
            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, level);

            URL url;
            String dir = type == RenderMapType.AERIAL ? "map_skyview" : "map_2d/2012tlq";
            String fileType = type == RenderMapType.AERIAL ? ".jpg" : ".png";

            try {
                url = new URL("http://map" + domain_num + ".daumcdn.net/" +
                        dir + "/L" + level + "/" + (tilePos[1] + tileDeltaY) + "/" + (tilePos[0] + tileDeltaX) + fileType
                );
                System.out.println(url.getPath());
            } catch (MalformedURLException e) {
                return null;
            }

            domain_num++;
            if(domain_num >= 4) domain_num = 0;
            return ImageIO.read(url);
        } catch(OutOfProjectionBoundsException exception) {
            return null;
        }
    }


}
