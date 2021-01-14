package com.mndk.mapdisp4bte.renderer;

import com.mndk.mapdisp4bte.map.ExternalMapRenderer;
import com.mndk.mapdisp4bte.map.MapTileManager;
import com.mndk.mapdisp4bte.map.RenderMapSource;
import com.mndk.mapdisp4bte.map.RenderMapType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

public class MapTileRenderer {



    public static float y = 4.1f;
    public static boolean drawTiles = false;
    public static RenderMapSource renderMapSource = RenderMapSource.KAKAO;
    public static RenderMapType renderMapType = RenderMapType.PLAIN_MAP;
    public static float opacity = 1f;



    public static void renderTiles(ExternalMapRenderer renderer, double px, double py, double pz) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.scale(1, 1, 1);

        int level = 0;

        // Iterate tiles around player
        for (int y = -2; y <= 2; y++) for (int x = -2; x <= 2; x++) {
            renderer.renderTile(
                    t, builder,
                    level, MapTileRenderer.renderMapType,
                    MapTileRenderer.y, MapTileRenderer.opacity,
                    px, py, pz,
                    x, y
            );
        }

        MapTileManager.getInstance().freeUnusedResourceLocations();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
