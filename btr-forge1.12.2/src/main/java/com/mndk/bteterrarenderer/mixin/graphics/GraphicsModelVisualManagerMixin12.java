package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GraphicsModel;
import com.mndk.bteterrarenderer.core.graphics.GraphicsModelVisualManager;
import com.mndk.bteterrarenderer.mod.util.mixin.graphics.GraphicsModelVisualManagerImpl12;
import lombok.experimental.UtilityClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.awt.image.BufferedImage;

@UtilityClass
@Mixin(value = GraphicsModelVisualManager.class, remap = false)
public class GraphicsModelVisualManagerMixin12 {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void preRender() {
        GraphicsModelVisualManagerImpl12.preRender();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public Object allocateAndGetTextureObject(BufferedImage image) {
        return GraphicsModelVisualManagerImpl12.allocateAndGetTextureObject(image);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        GraphicsModelVisualManagerImpl12.drawModel(model, px, py, pz, opacity);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void deleteTextureObject(Object textureObject) {
        GraphicsModelVisualManagerImpl12.deleteTexture(textureObject);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void postRender() {
        GraphicsModelVisualManagerImpl12.postRender();
    }
}
