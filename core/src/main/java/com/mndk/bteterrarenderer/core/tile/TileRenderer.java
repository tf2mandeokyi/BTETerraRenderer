package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.format.McCoordTransformer;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;

import javax.annotation.Nonnull;
import java.util.List;

public class TileRenderer {

    public static void renderTiles(@Nonnull DrawContextWrapper<?> drawContextWrapper, double px, double py, double pz) {
        if(!BTETerraRendererConfig.HOLOGRAM.isDoRender()) return;

        BTETerraRendererConfig.HologramConfig hologramConfig = BTETerraRendererConfig.HOLOGRAM;

        TileMapService tms = TileMapService.getSelected().getItem();
        if(tms == null) return;
        if(Projections.getHologramProjection() == null) return;

        double yDiff = hologramConfig.getFlatMapYAxis() - py;
        if(Math.abs(yDiff) >= hologramConfig.getYDiffLimit()) return;
        px += hologramConfig.getXAlign();
        pz += hologramConfig.getZAlign();

        drawContextWrapper.pushMatrix();
        McConnector.client().glGraphicsManager.glDisableCull();
        McConnector.client().glGraphicsManager.glEnableBlend();
        McConnector.client().glGraphicsManager.glSetAlphaBlendFunc();

        McCoord playerPos = new McCoord(px, (float) py, pz);
        List<GraphicsModel> models = tms.getModels(playerPos);
        McCoordTransformer mcCoordTransformer = tms.getPositionTransformer(playerPos);
        float opacity = (float) hologramConfig.getOpacity();
        for(GraphicsModel model : models) {
            model.drawAndRender(drawContextWrapper, mcCoordTransformer, opacity);
        }
        tms.cleanUp();

        McConnector.client().glGraphicsManager.glDisableBlend();
        McConnector.client().glGraphicsManager.glDefaultBlendFunc();
        McConnector.client().glGraphicsManager.glEnableCull();
        drawContextWrapper.popMatrix();
    }
}
