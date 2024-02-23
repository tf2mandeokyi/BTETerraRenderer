package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public class GlGraphicsManagerImpl implements GlGraphicsManager {

    public void glEnableTexture() {
        RenderSystem.enableTexture();
    }
    public void glDisableTexture() {
        RenderSystem.disableTexture();
    }
    public void glEnableCull() {
        RenderSystem.enableCull();
    }
    public void glDisableCull() {
        RenderSystem.disableCull();
    }
    public void glEnableBlend() {
        RenderSystem.enableBlend();
    }
    public void glDisableBlend() {
        RenderSystem.disableBlend();
    }
    public void glSetAlphaBlendFunc() {
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
    }
    public void glDefaultBlendFunc() {
        RenderSystem.defaultBlendFunc();
    }

    public void setPositionTexShader() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }
    public void setPositionColorShader() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }
    public void setPositionTexColorShader() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    }
    public void setShaderTexture(NativeTextureWrapper textureObject) {
        RenderSystem.setShaderTexture(0, textureObject.get());
    }

    @SneakyThrows
    public NativeTextureWrapper allocateAndGetTextureObject(BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        Identifier id = MinecraftClient.getInstance().getTextureManager()
                .registerDynamicTexture("bteterrarenderer-texture", texture);
        return new NativeTextureWrapper(id);
    }
    public void deleteTextureObject(NativeTextureWrapper textureObject) {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureObject.get());
    }

    public void glEnableScissorTest() {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._enableScissorTest();
    }
    public void glScissorBox(int x, int y, int width, int height) {
        RenderSystem.assertOnGameThreadOrInit();
        GlStateManager._scissorBox(x, y, width, height);
    }
    public void glDisableScissorTest() {
        RenderSystem.disableScissor();
    }

}
