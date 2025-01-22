package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import com.mndk.bteterrarenderer.util.IOUtil;
import lombok.SneakyThrows;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public class GlGraphicsManagerImpl extends GlGraphicsManager {

    public NativeTextureWrapper getMissingTextureObject() {
        return new NativeTextureWrapperImpl(MissingSprite.getMissingSpriteId());
    }
    @SneakyThrows
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        NativeImage nativeImage = NativeImage.read(IOUtil.imageToInputStream(image));
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        Identifier id = Identifier.of(modId, "dynamic-" + count);
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
        return new NativeTextureWrapperImpl(id);
    }
    protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(((NativeTextureWrapperImpl) textureObject).delegate);
    }

}
