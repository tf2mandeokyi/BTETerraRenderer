package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import java.awt.image.BufferedImage;

public class DummyGlGraphicsManager implements GlGraphicsManager {
    @Override public void glEnableTexture() {}
    @Override public void glDisableTexture() {}
    @Override public void glEnableCull() {}
    @Override public void glDisableCull() {}
    @Override public void glEnableBlend() {}
    @Override public void glDisableBlend() {}
    @Override public void glSetAlphaBlendFunc() {}
    @Override public void glDefaultBlendFunc() {}
    @Override public void setPositionTexShader() {}
    @Override public void setPositionColorShader() {}
    @Override public void setPositionTexColorShader() {}
    @Override public void setPositionTexColorNormalShader() {}
    @Override public void setShaderTexture(NativeTextureWrapper textureObject) {}
    @Override public void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {}
    @Override public void glEnableScissorTest() {}
    @Override public void glScissorBox(int x, int y, int width, int height) {}
    @Override public void glDisableScissorTest() {}

    @Override public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapper(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    }
    @Override public NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image) {
        return new NativeTextureWrapper(image);
    }
}
