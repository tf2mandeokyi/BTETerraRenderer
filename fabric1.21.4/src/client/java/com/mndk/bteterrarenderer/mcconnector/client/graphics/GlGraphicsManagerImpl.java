package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.util.IOUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public class GlGraphicsManagerImpl extends GlGraphicsManager {

    public void glEnableTexture() {}
    public void glDisableTexture() {}
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
        setShader(ShaderProgramKeys.POSITION_TEX);
    }
    public void setPosColorShader() {
        setShader(ShaderProgramKeys.POSITION_COLOR);
    }
    public void setPosTexColorShader() {
        setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
    }
    public void setPosColorTexLightNormalShader() {
        setShader(ShaderProgramKeys.RENDERTYPE_SOLID);
    }
    public void setShaderTexture(NativeTextureWrapper textureObject) {
        RenderSystem.setShaderTexture(0, textureObject.get());
    }
    private static void setShader(ShaderProgramKey key) {
        ShaderProgram shaderProgram = MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(key);
        RenderSystem.setShader(shaderProgram);
    }

    public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapper(MissingSprite.getMissingSpriteId());
    }
    @SneakyThrows
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        Identifier id = Identifier.of(modId, "dynamic-" + count);
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
