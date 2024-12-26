package com.mndk.bteterrarenderer.mcconnector.util;

import com.mndk.bteterrarenderer.util.BTRUtil;

import javax.annotation.Nonnull;

public class MinecraftObjectWrapper<T> {
    private final T delegate;

    protected MinecraftObjectWrapper(@Nonnull T delegate) {
        this.delegate = BTRUtil.uncheckedCast(delegate);
    }

    @Nonnull
    public T getWrapped() {
        return this.delegate;
    }
}
