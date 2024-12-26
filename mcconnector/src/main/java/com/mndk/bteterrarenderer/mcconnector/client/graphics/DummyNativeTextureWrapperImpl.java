package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

public class DummyNativeTextureWrapperImpl extends AbstractNativeTextureWrapper<BufferedImage> {
    public DummyNativeTextureWrapperImpl(@Nonnull BufferedImage delegate) {
        super(delegate);
    }
}
