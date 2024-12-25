package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.util.IOUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public class GlGraphicsManagerImpl extends GlGraphicsManager {

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

    public void setPosTexShader() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }
    public void setPosColorShader() {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }
    public void setPosTexColorShader() {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
    }
    public void setPosColorTexLightNormalShader() {
        RenderSystem.setShader(GameRenderer::getRenderTypeSolidShader);
    }
    public void setShaderTexture(NativeTextureWrapper textureObject) {
        RenderSystem.setShaderTexture(0, textureObject.get());
    }

    public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapper(MissingSprite.getMissingSpriteId());
    }
    @SneakyThrows
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        Identifier id = new Identifier(modId, "dynamic-" + count);
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
        return new NativeTextureWrapper(id);
    }
    public void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureObject.get());
    }

    public void glEnableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }
    public void glDisableScissor() {
        RenderSystem.disableScissor();
    }

}
