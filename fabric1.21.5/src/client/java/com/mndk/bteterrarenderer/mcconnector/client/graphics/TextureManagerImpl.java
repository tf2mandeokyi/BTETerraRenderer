package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.util.IOUtil;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

public class TextureManagerImpl extends TextureManager {

    protected NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapperImpl(MissingSprite.getMissingSpriteId(), 16, 16);
    }
    @SneakyThrows
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, @Nonnull BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(
                () -> modId + "-dynamic-" + count,
                nativeImage
        );
        Identifier id = Identifier.of(modId, "dynamic-" + count);
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
        return new NativeTextureWrapperImpl(id, width, height);
    }
    protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(((NativeTextureWrapperImpl) textureObject).delegate);
    }

}
