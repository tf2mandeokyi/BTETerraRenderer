package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.graphics.GlFactor;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.projection.Projections;

import java.util.function.BiConsumer;

public class TileRenderer {

    public static void renderTiles(double px, double py, double pz) {

        BTRConfigConnector config = BTRConfigConnector.INSTANCE;
        BTRConfigConnector.RenderSettingsConnector settings = config.getRenderSettings();
        TileMapService tms = BTRConfigConnector.getTileMapService();

        if(tms == null) return;
        if(Projections.getServerProjection() == null) return;

        double yDiff = settings.getYAxis() - py;
        if(Math.abs(yDiff) >= settings.getYDiffLimit()) {
            return;
        }

        GraphicsConnector.INSTANCE.glPushMatrix();
        GraphicsConnector.INSTANCE.glDisableCull();
        GraphicsConnector.INSTANCE.glEnableBlend();
        GraphicsConnector.INSTANCE.glBlendFunc(GlFactor.SRC_ALPHA, GlFactor.ONE_MINUS_SRC_ALPHA);

        GraphicsConnector.INSTANCE.glScale(1, 1, 1);

        int size = settings.getRadius() - 1;

        BiConsumer<Integer, Integer> drawTile = (dx, dy) -> {
            if(Math.abs(dx) > size || Math.abs(dy) > size) return;
            tms.renderTile(
                    settings.getRelativeZoomValue(),
                    config.getMapServiceCategory() + "." + config.getMapServiceId(),
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

        GraphicsConnector.INSTANCE.glDisableBlend();
        GraphicsConnector.INSTANCE.glEnableCull();
        GraphicsConnector.INSTANCE.glPopMatrix();
    }

}
