package com.mndk.kmap4bte.map.kakao;

import com.mndk.kmap4bte.map.CustomMapRenderer;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import com.mndk.kmap4bte.projection.wtm.WTMTileConverter;
import com.mndk.kmap4bte.renderer.MapRenderer;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class KakaoMapRenderer extends CustomMapRenderer {

    // Tile boundary matrix
    //
    // double[...][0: tileX_add, 1: tileY_add, 2: u, 3: v]
    private static final double[][] CORNERS = {
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
    public BufferedImage fetchMap(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int level, RenderMapType type) throws IOException {

        try {
            // Convert player position to tile coordinate
            double[] posResult = Projections.BTE.toGeo(playerX, playerZ);
            posResult = Projections.WTM.fromGeo(posResult[0], posResult[1]);
            int[] tilePos = WTMTileConverter.wtmToTile(posResult[0], posResult[1], level);

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



    @Override
    public void renderTile(
            Tessellator t, BufferBuilder builder,
            int level, RenderMapType type,
            double y, double opacity,
            double px, double py, double pz,
            int tileDeltaX, int tileDeltaY
    ) throws IOException {
        try {

            ResourceLocation resourceLocation = this.getMapResourceLocationByPlayerCoordinate(px, pz, tileDeltaX, tileDeltaY, level, type);

            // Convert player position to tile coordinate
            double[] posResult = Projections.BTE.toGeo(px, pz);
            posResult = Projections.WTM.fromGeo(posResult[0], posResult[1]);
            int[] tilePos = WTMTileConverter.wtmToTile(posResult[0], posResult[1], level);

            FMLClientHandler.instance().getClient().renderEngine.bindTexture(resourceLocation);

            // begin vertex
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            double[] temp;

            // Convert boundaries
            for (int i = 0; i < 4; i++) {

                // Convert tile coordinate to wtm coordinate
                temp = WTMTileConverter.tileToWTM(tilePos[0] + CORNERS[i][0] + tileDeltaX, tilePos[1] + CORNERS[i][1] + tileDeltaY, level);

                // Convert wtm coordinate to geo coordinate
                try {
                    temp = Projections.WTM.toGeo(temp[0], temp[1]);

                    // Convert geo coordinate to bte projection coordinate
                    temp = Projections.BTE.fromGeo(temp[0], temp[1]);

                    // Add vertex
                    builder.pos(temp[0] - px, y - py, temp[1] - pz)
                            .tex(CORNERS[i][2], CORNERS[i][3])
                            .color(1.f, 1.f, 1.f, MapRenderer.opacity)
                            .endVertex();
                } catch (OutOfProjectionBoundsException exception) {
                    // Skip rendering tile it it has projection error
                }
            }

            t.draw();
        } catch(OutOfProjectionBoundsException ignored) {}
    }


}
