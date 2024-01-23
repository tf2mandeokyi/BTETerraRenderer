package com.mndk.bteterrarenderer.mcconnector.graphics;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;

import java.awt.image.BufferedImage;

public abstract class GlGraphicsManager {

    public static final GlGraphicsManager INSTANCE = makeInstance();
    private static GlGraphicsManager makeInstance() {
        return MixinUtil.notOverwritten();
    }

    public abstract void glEnableTexture();
    public abstract void glDisableTexture();
    public abstract void glEnableCull();
    public abstract void glDisableCull();
    public abstract void glEnableBlend();
    public abstract void glDisableBlend();
    public abstract void glSetAlphaBlendFunc();
    public abstract void glDefaultBlendFunc();

    public abstract void setPositionTexShader();
    public abstract void setPositionColorShader();
    public abstract void setPositionTexColorShader();
    public abstract void setShaderTexture(NativeTextureWrapper textureObject);

    public abstract NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image);
    public abstract void deleteTextureObject(NativeTextureWrapper textureObject);

    public abstract void glEnableScissorTest();
    public abstract void glScissorBox(int x, int y, int width, int height);
    public abstract void glDisableScissorTest();
}
