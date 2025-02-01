package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.util.IOUtil;
import com.mojang.blaze3d.platform.NativeImage;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

public class TextureManagerImpl extends TextureManager {

    protected NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapperImpl(MissingTextureAtlasSprite.getLocation());
    }
    @SneakyThrows
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, @Nonnull BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        DynamicTexture texture = new DynamicTexture(nativeImage);
        ResourceLocation location = new ResourceLocation(modId, "dynamic-" + count);
        Minecraft.getInstance().getTextureManager().register(location, texture);
        return new NativeTextureWrapperImpl(location);
    }
    protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        Minecraft.getInstance().getTextureManager().release(((NativeTextureWrapperImpl) textureObject).delegate);
    }

}
