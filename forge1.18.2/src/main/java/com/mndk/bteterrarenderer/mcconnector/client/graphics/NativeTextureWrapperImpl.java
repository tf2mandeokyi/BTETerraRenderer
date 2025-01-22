package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class NativeTextureWrapperImpl extends AbstractNativeTextureWrapper {
    @Nonnull public final ResourceLocation delegate;
}
