package com.mndk.kmap4bte.renderer;

import com.mndk.kmap4bte.map.MapFetcher;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import com.mndk.kmap4bte.projection.Projections;
import com.mndk.kmap4bte.projection.wtm.WTMTileConverter;
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



    public static void renderTile(Tessellator t, BufferBuilder builder,               // Drawing components
                                  int tileX, int tileY, int level, RenderMapType type,      // Tile data
                                  double y,                                           // rendering y position
                                  double px, double py, double pz                     // Player position
                                 ) throws IOException {

        // Fetch image data
        ResourceLocation resourceLocation = MapFetcher.getKakaoMapRLByTileIndex(tileX, tileY, level, type);
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(resourceLocation);

        // begin vertex
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        double[] temp;
        double metersPerUnit = Projections.BTE.metersPerUnit();

        // Convert boundaries
        for(int i=0;i<4;i++) {

            // Convert tile coordinate to wtm coordinate
            temp = WTMTileConverter.tileToWTM(tileX + CORNERS[i][0], tileY + CORNERS[i][1], level);

            // Convert wtm coordinate to geo coordinate
            temp = Projections.WTM.toGeo(temp[0], temp[1]);

            // Convert geo coordinate to bte projection coordinate
            temp = Projections.BTE.fromGeo(temp[0], temp[1]);

            // Multiply projection coordinate by metersPerUnit
            temp[0] *= metersPerUnit; temp[1] *= -metersPerUnit;

            // Add vertex
            builder.pos(temp[0]-px, y-py, temp[1]-pz)
                   .tex(CORNERS[i][2], CORNERS[i][3])
                   .endVertex();
        }

        t.draw();

    }



    public static void renderTiles(double px, double py, double pz) throws IOException {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        GlStateManager.scale(1, 1, 1);

        // GlStateManager.translate(-px, -py, -pz);

        // Normal GlStateManager#translate() method would cause floating point error,
        // as it gets farther from (0, 0).
        // So instead of using it, we can just pass drawTile() the player's position
        // so that it can subtract it from its coordinate results by itself.
        // Fortunately, this doesn't cause floating point issue.

        int level = 1;

        double metersPerUnit = Projections.BTE.metersPerUnit();

        // Convert in-game coordinate to geo coordinate
        double[] posResult = Projections.BTE.toGeo(px / metersPerUnit, -pz / metersPerUnit);

        // Apply wtm projection on geo coordinate result
        posResult = Projections.WTM.fromGeo(posResult[0], posResult[1]);

        // Convert wtm coordinate to tile coordinate
        int[] tilePos = WTMTileConverter.wtmToTile(posResult[0], posResult[1], level);

        // Iterate tiles around player
        for(int y=-2;y<=2;y++) for(int x=-2;x<=2;x++) {
            MapRenderer.renderTile(
                    t, builder,
                    tilePos[0]+x, tilePos[1]+y, level, MapRenderer.renderMapType,
                    MapRenderer.y, px, py, pz);
        }

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
