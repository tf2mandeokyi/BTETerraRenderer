package com.mndk.bteterrarenderer.mcconnector.util;

import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;

public class ResourceLocationWrapperImpl extends MinecraftObjectWrapper<Identifier> implements ResourceLocationWrapper {
    public ResourceLocationWrapperImpl(@Nonnull Identifier delegate) {
        super(delegate);
    }
}
