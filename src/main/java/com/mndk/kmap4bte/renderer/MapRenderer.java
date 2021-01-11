package com.mndk.kmap4bte.renderer;

import com.mndk.kmap4bte.map.CustomMapRenderer;
import com.mndk.kmap4bte.map.RenderMapSource;
import com.mndk.kmap4bte.map.RenderMapType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

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



    public static void renderTiles(CustomMapRenderer renderer, double px, double py, double pz) throws IOException {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.scale(1, 1, 1);

        int level = 1;

        // Iterate tiles around player
        for (int y = -2; y <= 2; y++) for (int x = -2; x <= 2; x++) {
            renderer.renderTile(
                    t, builder,
                    level, MapRenderer.renderMapType,
                    MapRenderer.y, MapRenderer.opacity,
                    px, py, pz,
                    x, y
            );
        }

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
