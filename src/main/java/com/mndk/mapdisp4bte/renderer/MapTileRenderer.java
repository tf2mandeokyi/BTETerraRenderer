package com.mndk.mapdisp4bte.renderer;

import com.mndk.mapdisp4bte.ModConfig;
import com.mndk.mapdisp4bte.map.ExternalMapRenderer;
import com.mndk.mapdisp4bte.map.MapTileCache;
import com.mndk.mapdisp4bte.map.RenderMapType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

public class MapTileRenderer {

    public static void renderTiles(ExternalMapRenderer renderer, double px, double py, double pz) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.scale(1, 1, 1);

        int level = 0;

        try {
            RenderMapType type = RenderMapType.valueOf(ModConfig.mapType);

            // Iterate tiles around player
            for (int y = -2; y <= 2; y++)
                for (int x = -2; x <= 2; x++) {
                    renderer.renderTile(
                            t, builder,
                            level, type,
                            ModConfig.yLevel, (float) ModConfig.opacity,
                            px+ModConfig.xAlign, py, pz+ModConfig.zAlign,
                            x, y
                    );
                }
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        MapTileCache.instance.cleanup();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
