package com.mndk.bteterrarenderer.mcconnector.util;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class ResourceLocationWrapperImpl extends ResourceLocationWrapper<ResourceLocation> {
    public ResourceLocationWrapperImpl(@Nonnull ResourceLocation delegate) {
        super(delegate);
    }
}
