package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.ModelGraphicsManager;
import com.mndk.bteterrarenderer.mod.util.mixin.graphics.ModelGraphicsManagerImpl18;
import lombok.experimental.UtilityClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;

@UtilityClass
@Mixin(value = ModelGraphicsManager.class, remap = false)
public class ModelGraphicsManagerMixin18 {

    @Overwrite
    public void preRender() {
        ModelGraphicsManagerImpl18.preRender();
    }

    @Overwrite
    public int allocateAndUploadTexture(BufferedImage image) {
        return ModelGraphicsManagerImpl18.allocateAndUploadTexture(image);
    }

    @Overwrite
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        ModelGraphicsManagerImpl18.drawModel(poseStack, model, px, py, pz, opacity);
    }

    @Overwrite
    public void glDeleteTexture(int glId) {
        ModelGraphicsManagerImpl18.glDeleteTexture(glId);
    }

    @Overwrite
    public void postRender() {
        ModelGraphicsManagerImpl18.postRender();
    }
}
