package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelVisualManager;
import com.mndk.bteterrarenderer.mod.util.mixin.graphics.GraphicsModelVisualManagerImpl18;
import lombok.experimental.UtilityClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;

@UtilityClass
@Mixin(value = GraphicsModelVisualManager.class, remap = false)
public class GraphicsModelVisualManagerMixin18 {

    @Overwrite
    public void preRender() {
        GraphicsModelVisualManagerImpl18.preRender();
    }

    @Overwrite
    public int allocateAndUploadTexture(BufferedImage image) {
        return GraphicsModelVisualManagerImpl18.allocateAndUploadTexture(image);
    }

    @Overwrite
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        GraphicsModelVisualManagerImpl18.drawModel(poseStack, model, px, py, pz, opacity);
    }

    @Overwrite
    public void glDeleteTexture(int glId) {
        GraphicsModelVisualManagerImpl18.glDeleteTexture(glId);
    }

    @Overwrite
    public void postRender() {
        GraphicsModelVisualManagerImpl18.postRender();
    }
}
