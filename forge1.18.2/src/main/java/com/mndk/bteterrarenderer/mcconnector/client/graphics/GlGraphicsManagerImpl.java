package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.util.IOUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

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
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
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
        RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
    }
    public void setShaderTexture(NativeTextureWrapper textureObject) {
        RenderSystem.setShaderTexture(0, textureObject.get());
    }

    public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapper(MissingTextureAtlasSprite.getLocation());
    }
    @SneakyThrows
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation location = new ResourceLocation(modId, "dynamic-" + count);
        Minecraft.getInstance().getTextureManager().register(location, texture);
        return new NativeTextureWrapper(location);
    }
    public void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        Minecraft.getInstance().getTextureManager().release(textureObject.get());
    }

    public void glEnableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }
    public void glDisableScissor() {
        RenderSystem.disableScissor();
    }
}
