package com.mndk.bteterrarenderer.tile;

import com.mndk.bteterrarenderer.connector.ImplFinder;

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
    void drawTileQuad(TileQuad<TileQuad.PosTexColor> tileQuad);
    void glDeleteTileTexture(int glId);
    void postRender();
}
