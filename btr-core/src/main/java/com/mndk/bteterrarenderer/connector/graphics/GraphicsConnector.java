package com.mndk.bteterrarenderer.connector.graphics;

import com.mndk.bteterrarenderer.connector.ImplFinder;
import com.mndk.bteterrarenderer.connector.minecraft.IResourceLocation;

public interface GraphicsConnector {
    GraphicsConnector INSTANCE = ImplFinder.search();

    void glPushAttrib();
    void glPopAttrib();
    void glTranslate(float x, float y, float z);
    void glColor(float r, float g, float b, float a);
    void glPushMatrix();
    void glPopMatrix();
    void glEnableScissorTest();
    void glDisableScissorTest();
    void glEnableBlend();
    void glDisableBlend();
    void glEnableTexture2D();
    void glDisableTexture2D();
    void glRelativeScissor(int x, int y, int width, int height);
    void glTryBlendFuncSeparate(GlFactor srcFactor, GlFactor dstFactor, GlFactor srcFactorAlpha, GlFactor dstFactorAlpha);

    IBufferBuilder getBufferBuilder();
    void tessellatorDraw();
    void bindTexture(IResourceLocation res);
}
