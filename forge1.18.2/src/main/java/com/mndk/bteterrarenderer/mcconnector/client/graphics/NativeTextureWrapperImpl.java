package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class NativeTextureWrapperImpl extends AbstractNativeTextureWrapper<ResourceLocation> {
    public NativeTextureWrapperImpl(@Nonnull ResourceLocation delegate) {
        super(delegate);
    }
}
