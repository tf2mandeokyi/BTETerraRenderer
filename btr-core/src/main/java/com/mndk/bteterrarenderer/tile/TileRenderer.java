package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.graphics.ModelGraphicsConnector;
import com.mndk.bteterrarenderer.graphics.GraphicsModelManager;
import com.mndk.bteterrarenderer.projection.Projections;

public class TileRenderer {

    /**
     * This variable is to prevent z-fighting from happening.<br>
     * Setting this lower than 0.1 won't have its effect when the hologram is viewed far away from player
     */
    private static final double Y_EPSILON = 0.1;

    public static void renderTiles(Object poseStack, double px, double py, double pz) {
        if(!BTRConfigConnector.INSTANCE.isDoRender()) return;

        BTRConfigConnector config = BTRConfigConnector.INSTANCE;
        BTRConfigConnector.RenderSettingsConnector settings = config.getRenderSettings();

        FlatTileMapService tms = BTRConfigConnector.getTileMapService();
        if(tms == null) return;
        if(Projections.getServerProjection() == null) return;

        double yDiff = settings.getYAxis() - py;
        if(Math.abs(yDiff) >= settings.getYDiffLimit()) return;

        GraphicsConnector.INSTANCE.glPushMatrix(poseStack);
        ModelGraphicsConnector.INSTANCE.preRender();

        tms.render(
                poseStack,
                config.getMapServiceCategory() + "." + config.getMapServiceId(),
                px + settings.getXAlign(),
                py - (settings.getYAxis() + Y_EPSILON), // TODO: move this Y_EPSILON to FlatTileMapService
                pz + settings.getZAlign(),
                (float) settings.getOpacity()
        );
        GraphicsModelManager.INSTANCE.cleanup();

        ModelGraphicsConnector.INSTANCE.postRender();
        GraphicsConnector.INSTANCE.glPopMatrix(poseStack);
    }
}
