package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import java.awt.image.BufferedImage;

public class DummyGlGraphicsManager extends GlGraphicsManager {
    @Override public void glEnableTexture() {}
    @Override public void glDisableTexture() {}
    @Override public void glEnableCull() {}
    @Override public void glDisableCull() {}
    @Override public void glEnableBlend() {}
    @Override public void glDisableBlend() {}
    @Override public void glSetAlphaBlendFunc() {}
    @Override public void glDefaultBlendFunc() {}
    @Override public void setPosTexShader() {}
    @Override public void setPosColorShader() {}
    @Override public void setPosTexColorShader() {}
    @Override public void setPosColorTexLightNormalShader() {}
    @Override public void setShaderTexture(NativeTextureWrapper textureObject) {}
    @Override
    protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {}

    @Override public void glEnableScissor(int x, int y, int width, int height) {}
    @Override public void glDisableScissor() {}

    @Override public NativeTextureWrapper getMissingTextureObject() {
        return new DummyNativeTextureWrapperImpl(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    }
    @Override
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        return new DummyNativeTextureWrapperImpl(image);
    }
}
