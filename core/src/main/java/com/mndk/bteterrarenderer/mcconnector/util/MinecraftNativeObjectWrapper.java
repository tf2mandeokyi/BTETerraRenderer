package com.mndk.bteterrarenderer.mcconnector.util;

import com.mndk.bteterrarenderer.core.util.BTRUtil;

import javax.annotation.Nonnull;

public class MinecraftNativeObjectWrapper<T> {
    private final T delegate;

    protected MinecraftNativeObjectWrapper(@Nonnull T delegate) {
        this.delegate = BTRUtil.uncheckedCast(delegate);
    }

    @Nonnull
    protected T getThisWrapped() {
        return this.delegate;
    }

    @Nonnull
    public <U> U get() {
        return BTRUtil.uncheckedCast(this.delegate);
    }
}
