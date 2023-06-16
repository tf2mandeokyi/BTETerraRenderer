package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.projection.Projections;
import lombok.RequiredArgsConstructor;

public class TileRenderer {

    /**
     * This variable is to prevent z-fighting from happening.<br>
     * Setting this lower than 0.1 won't have its effect when the hologram is viewed far away from player
     */
    private static final double Y_EPSILON = 0.1;

    public static void renderTiles(Object poseStack, double px, double py, double pz) {

        BTRConfigConnector config = BTRConfigConnector.INSTANCE;
        BTRConfigConnector.RenderSettingsConnector settings = config.getRenderSettings();

        TileMapService tms = BTRConfigConnector.getTileMapService();
        if(tms == null) return;
        if(Projections.getServerProjection() == null) return;

        double yDiff = settings.getYAxis() - py;
        if(Math.abs(yDiff) >= settings.getYDiffLimit()) return;

        GraphicsConnector.INSTANCE.glPushMatrix(poseStack);
        TileGraphicsConnector.INSTANCE.preRender();

        new TileDrawer(tms, settings.getRadius() - 1, px, py, pz).drawWithDiamondPriority(poseStack);
        TileImageCacheManager.getInstance().cleanup();

        TileGraphicsConnector.INSTANCE.postRender();
        GraphicsConnector.INSTANCE.glPopMatrix(poseStack);
    }


    @RequiredArgsConstructor
    private static class TileDrawer {

        private final TileMapService tms;
        private final int size;
        private final double px, py, pz;
        private final BTRConfigConnector config = BTRConfigConnector.INSTANCE;
        private final BTRConfigConnector.RenderSettingsConnector settings = config.getRenderSettings();

        public void draw(Object poseStack, int dx, int dy) {
            if(Math.abs(dx) > size || Math.abs(dy) > size) return;
            tms.renderTile(
                    poseStack,
                    settings.getRelativeZoomValue(),
                    config.getMapServiceCategory() + "." + config.getMapServiceId(),
                    settings.getYAxis() + Y_EPSILON,
                    (float) settings.getOpacity(),
                    px + settings.getXAlign(), py, pz + settings.getZAlign(),
                    dx, dy
            );
        }

        public void drawWithDiamondPriority(Object poseStack) {
            for(int i = 0; i < 2 * this.size + 1; ++i) {
                if(i == 0) {
                    this.draw(poseStack, 0, 0);
                }
                for(int j = 0; j < i; ++j) {
                    this.draw(poseStack, -j, j - i);
                    this.draw(poseStack, j - i, j);
                    this.draw(poseStack, j, i - j);
                    this.draw(poseStack, i - j, -j);
                }
            }
        }
    }
}
