package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class GlGraphicsManagerImpl implements GlGraphicsManager {
    public void glEnableTexture() {
        GlStateManager.enableTexture2D();
    }
    public void glDisableTexture() {
        GlStateManager.disableTexture2D();
    }
    public void glEnableCull() {
        GlStateManager.enableCull();
    }
    public void glDisableCull() {
        GlStateManager.disableCull();
    }
    public void glEnableBlend() {
        GlStateManager.enableBlend();
    }
    public void glDisableBlend() {
        GlStateManager.disableBlend();
    }
    public void glSetAlphaBlendFunc() {
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }
    public void glDefaultBlendFunc() {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public void setPositionTexShader() {}
    public void setPositionColorShader() {}
    public void setPositionTexColorShader() {}
    public void setPositionTexColorNormalShader() {}
    public void setShaderTexture(NativeTextureWrapper textureObject) {
        GlStateManager.bindTexture(textureObject.get());
    }

    public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapper(TextureUtil.MISSING_TEXTURE.getGlTextureId());
    }
    public NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image) {
        int glId = GL11.glGenTextures();
        int width = image.getWidth(), height = image.getHeight();
        TextureUtil.allocateTexture(glId, width, height);

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);
        TextureUtil.uploadTexture(glId, imageData, width, height);
        return new NativeTextureWrapper(glId);
    }
    public void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        GlStateManager.deleteTexture(textureObject.get());
    }

    public void glEnableScissorTest() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }
    public void glScissorBox(int x, int y, int width, int height) {
        GL11.glScissor(x, y, width, height);
    }
    public void glDisableScissorTest() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
