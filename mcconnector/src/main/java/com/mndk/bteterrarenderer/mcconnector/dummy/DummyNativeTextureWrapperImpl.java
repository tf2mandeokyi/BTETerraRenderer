package com.mndk.bteterrarenderer.mcconnector.dummy;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.AbstractNativeTextureWrapper;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

@RequiredArgsConstructor
public class DummyNativeTextureWrapperImpl extends AbstractNativeTextureWrapper {
    @Nonnull public final BufferedImage delegate;
}
