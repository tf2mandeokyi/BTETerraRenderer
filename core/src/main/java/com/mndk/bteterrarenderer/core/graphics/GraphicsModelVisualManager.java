package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.core.graphics.model.GraphicsModel;
import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import java.awt.image.BufferedImage;

@UtilityClass
public class GraphicsModelVisualManager {
    public void preRender() {
        MixinUtil.notOverwritten();
    }
    /**
     * Allocates given buffered image.
     * @param image The buffered image
     * @return Corresponding glId
     */
    public Object allocateAndGetTextureObject(BufferedImage image) {
        return MixinUtil.notOverwritten(image);
    }
    public void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity) {
        MixinUtil.notOverwritten(poseStack, model, px, py, pz, opacity);
    }
    public void deleteTextureObject(Object textureObject) {
        MixinUtil.notOverwritten(textureObject);
    }
    public void postRender() {
        MixinUtil.notOverwritten();
    }
}
