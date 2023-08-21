package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import java.awt.image.BufferedImage;

@UtilityClass
public class ModelGraphicsManager {
    public void preRender() {
        MixinUtil.notOverwritten();
    }
    /**
     * Allocates given buffered image.
     * @param image The buffered image
     * @return Corresponding glId
     */
    public int allocateAndUploadTexture(BufferedImage image) {
        return MixinUtil.notOverwritten(image);
    }
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        MixinUtil.notOverwritten(poseStack, model, px, py, pz, opacity);
    }
    public void glDeleteTexture(int glId) {
        MixinUtil.notOverwritten(glId);
    }
    public void postRender() {
        MixinUtil.notOverwritten();
    }
}
