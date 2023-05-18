package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.graphics.GraphicVertices;

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
    void drawTileQuad(GraphicVertices<GraphicVertices.PosTexColor> vertices);
    void glDeleteTileTexture(int glId);
    void postRender();
}
