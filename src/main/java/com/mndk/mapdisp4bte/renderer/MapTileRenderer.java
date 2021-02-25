package com.mndk.mapdisp4bte.renderer;

import com.mndk.mapdisp4bte.config.ConfigHandler;
import com.mndk.mapdisp4bte.config.ModConfig;
import com.mndk.mapdisp4bte.map.ExternalMapManager;
import com.mndk.mapdisp4bte.map.MapTileManager;
import com.mndk.mapdisp4bte.map.RenderMapType;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;

public class MapTileRenderer {

    public static void renderTiles(ExternalMapManager renderer, double px, double py, double pz) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder builder = t.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.scale(1, 1, 1);

        ModConfig config = ConfigHandler.getModConfig();

        try {
            RenderMapType type = config.getMapType();

            // Iterate tiles around player
            for (int y = -2; y <= 2; y++)
                for (int x = -2; x <= 2; x++) {
                    renderer.renderTile(
                            t, builder,
                            config.getZoom(), type,
                            config.getYLevel() + 0.1, (float) config.getOpacity(), // Adding .1 to y because rendering issue
                            px+config.getXAlign(), py, pz+config.getZAlign(),
                            x, y
                    );
                }
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }

        MapTileManager.getInstance().getTileCache().cleanup();

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }
}
