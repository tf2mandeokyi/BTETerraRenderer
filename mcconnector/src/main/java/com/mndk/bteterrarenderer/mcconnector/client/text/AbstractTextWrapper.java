package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class AbstractTextWrapper<T> extends MinecraftObjectWrapper<T> implements TextWrapper {
    protected AbstractTextWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    public final List<? extends TextWrapper> splitByWidth(FontWrapper fontWrapper, int wrapWidth) {
        return this.splitByWidthUnsafe(fontWrapper, Math.max(wrapWidth, 1));
    }

    protected abstract List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth);
}
