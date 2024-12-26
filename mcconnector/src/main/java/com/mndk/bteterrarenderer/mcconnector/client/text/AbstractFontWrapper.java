package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftObjectWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public abstract class AbstractFontWrapper<T> extends MinecraftObjectWrapper<T> implements FontWrapper {

    protected AbstractFontWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    protected abstract List<String> splitByWidthUnsafe(String string, int wrapWidth);
    public final int getWordWrappedHeight(String text, int maxWidth) {
        return this.getHeight() * this.splitByWidth(text, maxWidth).size();
    }
    public final List<String> splitByWidth(String string, int wrapWidth) {
        return this.splitByWidthUnsafe(string, Math.max(wrapWidth, 1));
    }

    public final int getWidth(TextWrapper text) {
        return text.getWidth(this);
    }
    public final List<? extends TextWrapper> splitByWidth(TextWrapper text, int wrapWidth) {
        return text.splitByWidth(this, wrapWidth);
    }
    @Nullable
    public final StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
        return textWrapper.getStyleComponentFromLine(this, mouseXFromLeft);
    }
}
