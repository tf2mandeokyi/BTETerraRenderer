package com.mndk.bteterrarenderer.mcconnector.wrapper;

import com.mndk.bteterrarenderer.core.util.BTRUtil;

import javax.annotation.Nonnull;

public class MinecraftNativeObjectWrapper {
    private final Object delegate;

    protected MinecraftNativeObjectWrapper(@Nonnull Object delegate) {
        this.delegate = delegate;
    }

    @Nonnull
    public <T> T get() {
        return BTRUtil.uncheckedCast(this.delegate);
    }
}
