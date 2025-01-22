package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.util.IOUtil;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.awt.image.BufferedImage;

public class GlGraphicsManagerImpl extends GlGraphicsManager {

    public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapperImpl(MissingTextureAtlasSprite.getLocation());
    }
    @SneakyThrows
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation location = new ResourceLocation(modId, "dynamic-" + count);
        Minecraft.getInstance().getTextureManager().register(location, texture);
        return new NativeTextureWrapperImpl(location);
    }
    protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        Minecraft.getInstance().getTextureManager().release(((NativeTextureWrapperImpl) textureObject).delegate);
    }

    public void glEnableScissor(int x, int y, int width, int height) {
        RenderSystem.enableScissor(x, y, width, height);
    }
    public void glDisableScissor() {
        RenderSystem.disableScissor();
    }
}
