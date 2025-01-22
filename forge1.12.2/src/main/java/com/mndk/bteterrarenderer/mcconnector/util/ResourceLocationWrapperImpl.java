package com.mndk.bteterrarenderer.mcconnector.util;

import lombok.RequiredArgsConstructor;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class ResourceLocationWrapperImpl implements ResourceLocationWrapper {
    @Nonnull public final ResourceLocation delegate;
}