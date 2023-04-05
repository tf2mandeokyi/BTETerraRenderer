package com.mndk.bteterrarenderer.connector.minecraft.graphics;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;

public interface GraphicsConnector {
    GraphicsConnector INSTANCE = ImplFinder.search(GraphicsConnector.class);

    int glGenTextures();
    void allocateTexture(int glId, int width, int height);
    void uploadTexture(int glId, int[] imageData, int width, int height);
    void glBindTexture(int glId);
    void glDeleteTexture(int glId);
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

    BufferBuilderConnector getBufferBuilder();
    void tessellatorDraw();
    void bindTexture(IResourceLocation res);
}
