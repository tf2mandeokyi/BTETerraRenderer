package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class NativeTextureWrapperImpl extends AbstractNativeTextureWrapper {
    @Nonnull public final Identifier delegate;
}
