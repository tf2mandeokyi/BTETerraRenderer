package com.mndk.bteterrarenderer.mcconnector.dummy;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.TextureManager;

import java.awt.image.BufferedImage;

public class DummyTextureManager extends TextureManager {
    @Override protected void deleteTextureObjectInternal(NativeTextureWrapper textureObject) {}

    @Override public NativeTextureWrapper getMissingTextureObject() {
        return new DummyNativeTextureWrapperImpl(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    }
    @Override
    protected NativeTextureWrapper allocateAndGetTextureObject(String modId, int count, BufferedImage image) {
        return new DummyNativeTextureWrapperImpl(image);
    }
}
