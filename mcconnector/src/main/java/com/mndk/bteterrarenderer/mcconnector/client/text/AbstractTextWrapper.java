package com.mndk.bteterrarenderer.mcconnector.client.text;

import java.util.List;

public abstract class AbstractTextWrapper implements TextWrapper {

    public final List<? extends TextWrapper> splitByWidth(FontWrapper fontWrapper, int wrapWidth) {
        return this.splitByWidthUnsafe(fontWrapper, Math.max(wrapWidth, 1));
    }

    protected abstract List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth);
}
