package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class TextureManagerImpl extends TextureManager {

    public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapperImpl(TextureUtil.MISSING_TEXTURE.getGlTextureId());
    }
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        int glId = GL11.glGenTextures();
        int width = image.getWidth(), height = image.getHeight();
        TextureUtil.allocateTexture(glId, width, height);

        int[] imageData = new int[width * height];
        image.getRGB(0, 0, width, height, imageData, 0, width);
        TextureUtil.uploadTexture(glId, imageData, width, height);
        return new NativeTextureWrapperImpl(glId);
    }
    protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        GlStateManager.deleteTexture(((NativeTextureWrapperImpl) textureObject).delegate);
    }

}
