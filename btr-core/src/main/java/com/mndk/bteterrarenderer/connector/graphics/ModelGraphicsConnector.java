package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.graphics.GraphicsModel;

import java.awt.image.BufferedImage;

public interface ModelGraphicsConnector {
    ModelGraphicsConnector INSTANCE = ImplFinder.search();

    void preRender();
    /**
     * Allocates given buffered image.
     * @param image The buffered image
     * @return Corresponding glId
     */
    int allocateAndUploadTexture(BufferedImage image);
    void drawModel(Object poseStack, GraphicsModel model, double px, double py, double pz, float opacity);
    void glDeleteTexture(int glId);
    void postRender();
}
