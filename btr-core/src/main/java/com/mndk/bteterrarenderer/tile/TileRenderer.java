package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.connector.Connectors;
import com.mndk.bteterrarenderer.connector.minecraft.graphics.GlFactor;
import com.mndk.bteterrarenderer.projection.Projections;

import java.util.function.BiConsumer;

public class TileRenderer {

    public static void renderTiles(String tmsId, TileMapService tms, double px, double py, double pz) {

        if(tms == null) return;
        if(Projections.getServerProjection() == null) return;

        BTETerraRendererConfig.RenderSettings settings = BTETerraRendererCore.CONFIG.renderSettings;

        double yDiff = settings.getYAxis() - py;
        if(Math.abs(yDiff) >= settings.getYDiffLimit()) {
            return;
        }

        Connectors.GRAPHICS.glPushMatrix();
        Connectors.GRAPHICS.glDisableCull();
        Connectors.GRAPHICS.glEnableBlend();
        Connectors.GRAPHICS.glBlendFunc(GlFactor.SRC_ALPHA, GlFactor.ONE_MINUS_SRC_ALPHA);

        Connectors.GRAPHICS.glScale(1, 1, 1);

        int size = settings.getRadius() - 1;

        BiConsumer<Integer, Integer> drawTile = (dx, dy) -> {
            if(Math.abs(dx) > size || Math.abs(dy) > size) return;
            tms.renderTile(
                    settings.getZoom(), tmsId,
                    settings.getYAxis() + 0.1, // Adding .1 to y because of texture-overlapping issue
                    (float) settings.getOpacity(),
                    px + settings.getXAlign(), py, pz + settings.getZAlign(),
                    dx, dy
            );
        };

        // Draw tiles around player with diamond-priority
        for(int i = 0; i < 2 * size + 1; ++i) {
            if(i == 0) {
                drawTile.accept(0, 0);
            }
            for(int j = 0; j < i; ++j) {
                drawTile.accept(-j, j - i);
                drawTile.accept(j - i, j);
                drawTile.accept(j, i - j);
                drawTile.accept(i - j, -j);
            }
        }

        TileImageCacheManager.getInstance().cleanup();

        Connectors.GRAPHICS.glDisableBlend();
        Connectors.GRAPHICS.glEnableCull();
        Connectors.GRAPHICS.glPopMatrix();
    }

}
