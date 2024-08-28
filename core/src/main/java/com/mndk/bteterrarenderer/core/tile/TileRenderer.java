package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

import javax.annotation.Nonnull;

public class TileRenderer {

    public static void renderTiles(@Nonnull DrawContextWrapper<?> drawContextWrapper, double px, double py, double pz) {
        if(!BTETerraRendererConfig.HOLOGRAM.isDoRender()) return;

        BTETerraRendererConfig.HologramConfig hologramConfig = BTETerraRendererConfig.HOLOGRAM;

        TileMapService tms = TileMapService.getSelected().getItem();
        if(tms == null) return;
        if(Projections.getHologramProjection() == null) return;

        double yDiff = hologramConfig.getFlatMapYAxis() - py;
        if(Math.abs(yDiff) >= hologramConfig.getYDiffLimit()) return;

        drawContextWrapper.pushMatrix();
        McConnector.client().glGraphicsManager.glDisableCull();
        McConnector.client().glGraphicsManager.glEnableBlend();
        McConnector.client().glGraphicsManager.glSetAlphaBlendFunc();

        tms.render(drawContextWrapper,
                px + hologramConfig.getXAlign(), py, pz + hologramConfig.getZAlign(),
                (float) hologramConfig.getOpacity());
        tms.cleanUp();

        McConnector.client().glGraphicsManager.glDisableBlend();
        McConnector.client().glGraphicsManager.glDefaultBlendFunc();
        McConnector.client().glGraphicsManager.glEnableCull();
        drawContextWrapper.popMatrix();
    }
}
