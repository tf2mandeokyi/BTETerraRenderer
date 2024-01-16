package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;
import com.mndk.bteterrarenderer.mcconnector.wrapper.NativeTextureWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;

@RequiredArgsConstructor
public class ImageTexturePair {
    private final BufferedImage image;
    @Getter
    private NativeTextureWrapper textureObject = null;

    public void bake() {
        if (this.textureObject != null) return;
        this.textureObject = GlGraphicsManager.INSTANCE.allocateAndGetTextureObject(this.image);
    }
}
