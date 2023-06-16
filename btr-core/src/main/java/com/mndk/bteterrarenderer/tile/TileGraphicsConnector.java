package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.graphics.GraphicsQuad;

import java.awt.image.BufferedImage;

public interface TileGraphicsConnector {
    TileGraphicsConnector INSTANCE = ImplFinder.search();

    void preRender();
    /**
     * Allocates given buffered image.
     * @param image The buffered image
     * @return Corresponding glId
     */
    int allocateAndUploadTileTexture(BufferedImage image);
    void drawTileQuad(Object poseStack, GraphicsQuad<GraphicsQuad.PosTexColor> quad);
    void glDeleteTileTexture(int glId);
    void postRender();
}
