package com.mndk.bteterrarenderer.mcconnector.client.graphics;

import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

public class NativeTextureWrapperImpl extends AbstractNativeTextureWrapper<Identifier> {
    public NativeTextureWrapperImpl(@Nonnull Identifier delegate) {
        super(delegate);
    }
}
