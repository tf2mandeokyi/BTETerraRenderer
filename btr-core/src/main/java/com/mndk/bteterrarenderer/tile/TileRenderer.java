package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsConnector;
import com.mndk.bteterrarenderer.connector.graphics.ModelGraphicsConnector;
import com.mndk.bteterrarenderer.graphics.GraphicsModelManager;
import com.mndk.bteterrarenderer.projection.Projections;

public class TileRenderer {

    public static void renderTiles(Object poseStack, double px, double py, double pz) {
        if(!BTRConfigConnector.INSTANCE.isDoRender()) return;

        BTRConfigConnector config = BTRConfigConnector.INSTANCE;
        BTRConfigConnector.RenderSettingsConnector settings = config.getRenderSettings();

        TileMapService tms = BTRConfigConnector.getTileMapService().getValue();
        if(tms == null) return;
        if(Projections.getServerProjection() == null) return;

        double yDiff = settings.getFlatMapYAxis() - py;
        if(Math.abs(yDiff) >= settings.getYDiffLimit()) return;

        GraphicsConnector.INSTANCE.glPushMatrix(poseStack);
        ModelGraphicsConnector.INSTANCE.preRender();

        tms.render(
                poseStack,
                config.getMapServiceCategory() + "." + config.getMapServiceId(),
                px + settings.getXAlign(),
                py,
                pz + settings.getZAlign(),
                (float) settings.getOpacity()
        );
        GraphicsModelManager.INSTANCE.cleanup();

        ModelGraphicsConnector.INSTANCE.postRender();
        GraphicsConnector.INSTANCE.glPopMatrix(poseStack);
    }
}
