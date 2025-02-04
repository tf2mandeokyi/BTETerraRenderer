package com.mndk.bteterrarenderer.mcconnector.dummy;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.TextureManager;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

public class DummyTextureManager extends TextureManager {
    @Override protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {}

    @Override
    protected NativeTextureWrapper getMissingTextureObject() {
        return new DummyNativeTextureWrapperImpl(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), 1, 1);
    }
    @Override
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, @Nonnull BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        return new DummyNativeTextureWrapperImpl(image, width, height);
    }
}
