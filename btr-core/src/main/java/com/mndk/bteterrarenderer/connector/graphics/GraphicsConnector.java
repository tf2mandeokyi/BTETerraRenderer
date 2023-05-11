package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.tile.TileQuad;

import java.awt.image.BufferedImage;

public interface GraphicsConnector {
    GraphicsConnector INSTANCE = ImplFinder.search();

    /**
     * Allocates given buffered image.
     * @param image The buffered image
     * @return Corresponding glId
     */
    int allocateAndUploadTileTexture(BufferedImage image);
    void drawTileQuad(TileQuad tileQuad);
    void glDeleteTileTexture(int glId);
    void glPushAttrib();
    void glPopAttrib();
    void glTranslate(float x, float y, float z);
    void glScale(float x, float y, float z);
    void glColor(float r, float g, float b, float a);
    void glPushMatrix();
    void glPopMatrix();
    void glEnableScissorTest();
    void glDisableScissorTest();
    void glEnableCull();
    void glDisableCull();
    void glEnableBlend();
    void glDisableBlend();
    void glEnableTexture2D();
    void glDisableTexture2D();
    void glRelativeScissor(int x, int y, int width, int height);
    void glBlendFunc(GlFactor srcFactor, GlFactor dstFactor);
    void glTryBlendFuncSeparate(GlFactor srcFactor, GlFactor dstFactor, GlFactor srcFactorAlpha, GlFactor dstFactorAlpha);

    IBufferBuilder getBufferBuilder();
    void tessellatorDraw();
    void bindTexture(IResourceLocation res);
}
