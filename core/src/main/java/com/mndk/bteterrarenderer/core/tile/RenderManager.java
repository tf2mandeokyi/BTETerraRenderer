package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.VertexBeginner;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.WorldDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import com.mndk.bteterrarenderer.mcconnector.util.math.McCoordTransformer;

import javax.annotation.Nonnull;
import java.util.List;

public class RenderManager {

    public static void renderTiles(@Nonnull WorldDrawContextWrapper context, double px, double py, double pz) {
        if (!BTETerraRendererConfig.HOLOGRAM.isDoRender()) return;

        TileMapService tms = TileMapService.getSelected().getItem();
        if (tms == null) return;

        BTETerraRendererConfig.HologramConfig hologramConfig = BTETerraRendererConfig.HOLOGRAM;
        double yDiff = hologramConfig.getFlatMapYAxis() - py;
        if (Math.abs(yDiff) >= hologramConfig.getYDiffLimit()) return;
        px += hologramConfig.getXAlign();
        pz += hologramConfig.getZAlign();

        double yawDegrees = McConnector.client().getPlayerRotationYaw();
        double pitchDegrees = McConnector.client().getPlayerRotationPitch();
        McCoord cameraPos = new McCoord(px, (float) py, pz);
        List<GraphicsModel> models = tms.getModels(cameraPos, yawDegrees, pitchDegrees);
        McCoordTransformer transformer = tms.getModelPositionTransformer();
        McCoordTransformer modelPosTransformer = pos -> transformer.transform(pos).subtract(cameraPos);
        VertexBeginner beginner = tms.getVertexBeginner(
                McConnector.client().bufferBuildersManager,
                (float) hologramConfig.getOpacity()
        );
        for (GraphicsModel model : models) {
            model.drawAndRender(context, modelPosTransformer, beginner);
        }
        tms.cleanUp();
    }

    public static void renderHud(@Nonnull GuiDrawContextWrapper context) {
        if (!BTETerraRendererConfig.HOLOGRAM.isDoRender()) return;

        TileMapService tms = TileMapService.getSelected().getItem();
        if (tms != null) tms.renderHud(context);
    }
}
