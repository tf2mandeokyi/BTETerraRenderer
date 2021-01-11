package com.mndk.kmap4bte.renderer;

import com.mndk.kmap4bte.map.MapFetcher;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import com.mndk.kmap4bte.projection.wtm.WTMTileConverter;
import io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class MapRenderer {

    // Tile boundary matrix
    //
    // double[...][0: tileX_add, 1: tileY_add, 2: u, 3: v]
    private static final double[][] CORNERS = {
            {0, 1, 0, 0}, // top left
            {1, 1, 1, 0}, // top right
            {1, 0, 1, 1}, // bottom right
            {0, 0, 0, 1}  // bottom left
    };



    public static float y = 4.1f;
    public static boolean drawTiles = false;
    public static RenderMapSource renderMapSource = RenderMapSource.KAKAO;
    public static RenderMapType renderMapType = RenderMapType.PLAIN_MAP;
    public static float opacity = 1f;



    public static void renderTile(Tessellator t, BufferBuilder builder,               // Drawing components
                                  int tileX, int tileY, int level, RenderMapType type,      // Tile data
                                  double y,                                           // rendering y position
                                  double px, double py, double pz                     // Player position
                                 ) throws IOException {

        // Fetch image data
        ResourceLocation resourceLocation = MapFetcher.getKakaoMapRLByTileIndex(tileX, tileY, level, type);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(resourceLocation);

        // begin vertex
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        double[] temp;

        // Convert boundaries
        for(int i=0;i<4;i++) {

            // Convert tile coordinate to wtm coordinate
            temp = WTMTileConverter.tileToWTM(tileX + CORNERS[i][0], tileY + CORNERS[i][1], level);

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
            } catch(OutOfProjectionBoundsException exception) {
                // Skip rendering tile it it has projection error
            }
        }

        t.draw();

    }



    public static void renderTiles(double px, double py, double pz) throws IOException {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.scale(1, 1, 1);

        int level = 1;

        try {
            // Convert in-game coordinate to geo coordinate
            double[] posResult = Projections.BTE.toGeo(px, pz);

            // Apply wtm projection on geo coordinate result
            posResult = Projections.WTM.fromGeo(posResult[0], posResult[1]);

            // Convert wtm coordinate to tile coordinate
            int[] tilePos = WTMTileConverter.wtmToTile(posResult[0], posResult[1], level);

            // Iterate tiles around player
            for (int y = -2; y <= 2; y++)
                for (int x = -2; x <= 2; x++) {
                    MapRenderer.renderTile(
                            t, builder,
                            tilePos[0] + x, tilePos[1] + y, level, MapRenderer.renderMapType,
                            MapRenderer.y, px, py, pz);
                }
        } catch(OutOfProjectionBoundsException exception) {
            // If there's projection error, then just do nothing :troll:
        }

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
