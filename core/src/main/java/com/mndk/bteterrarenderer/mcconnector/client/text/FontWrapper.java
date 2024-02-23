package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.util.MinecraftNativeObjectWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public abstract class FontWrapper<T> extends MinecraftNativeObjectWrapper<T> {

    protected FontWrapper(@Nonnull T delegate) {
        super(delegate);
    }

    public abstract int getHeight();
    public abstract int getWidth(String string);
    public abstract int getWidth(TextWrapper text);
    public abstract String trimToWidth(String string, int width);
    protected abstract List<String> splitByWidthUnsafe(String string, int wrapWidth);
    protected abstract List<TextWrapper> splitByWidthUnsafe(TextWrapper text, int wrapWidth);
    @Nullable
    public abstract StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft);

    public final List<String> splitByWidth(String string, int wrapWidth) {
        return this.splitByWidthUnsafe(string, Math.max(wrapWidth, 1));
    }
    public final List<TextWrapper> splitByWidth(TextWrapper text, int wrapWidth) {
        return this.splitByWidthUnsafe(text, Math.max(wrapWidth, 1));
    }

    public int getWordWrappedHeight(String text, int maxWidth) {
        return this.getHeight() * this.splitByWidth(text, maxWidth).size();
    }
}
