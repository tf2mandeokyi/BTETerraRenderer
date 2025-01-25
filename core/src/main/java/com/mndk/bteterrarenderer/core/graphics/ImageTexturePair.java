package com.mndk.bteterrarenderer.core.graphics;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.mcconnector.McConnector;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.NativeTextureWrapper;
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
        this.textureObject = McConnector.client().textureManager.allocateAndGetTextureObject(BTETerraRenderer.MODID, this.image);
    }
}
