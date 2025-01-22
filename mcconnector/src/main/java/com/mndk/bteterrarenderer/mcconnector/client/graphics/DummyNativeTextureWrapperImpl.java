package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

@RequiredArgsConstructor
public class DummyNativeTextureWrapperImpl extends AbstractNativeTextureWrapper {
    @Nonnull public final BufferedImage delegate;
}
