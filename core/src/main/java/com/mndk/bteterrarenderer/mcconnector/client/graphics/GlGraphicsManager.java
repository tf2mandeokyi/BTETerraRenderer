package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import java.awt.image.BufferedImage;

public interface GlGraphicsManager {
    void glEnableTexture();
    void glDisableTexture();
    void glEnableCull();
    void glDisableCull();
    void glEnableBlend();
    void glDisableBlend();
    void glSetAlphaBlendFunc();
    void glDefaultBlendFunc();

    void setPositionTexShader();
    void setPositionColorShader();
    void setPositionTexColorShader();
    void setPositionTexColorNormalShader();
    void setShaderTexture(NativeTextureWrapper textureObject);

    NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image);
    void deleteTextureObject(NativeTextureWrapper textureObject);

    void glEnableScissorTest();
    void glScissorBox(int x, int y, int width, int height);
    void glDisableScissorTest();
}
