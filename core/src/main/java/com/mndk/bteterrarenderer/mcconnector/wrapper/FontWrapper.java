package com.mndk.bteterrarenderer.mcconnector.wrapper;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.MixinUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public abstract class FontWrapper<T> extends MinecraftNativeObjectWrapper<T> {

    public static final FontWrapper<Object> DEFAULT = BTRUtil.uncheckedCast(makeDefault());
    private static FontWrapper<?> makeDefault() {
        return MixinUtil.notOverwritten();
    }

    protected FontWrapper(@Nonnull Object delegate) {
        super(delegate);
    }

    public abstract int getHeight();
    public abstract int getWidth(String string);
    public abstract int getWidth(TextWrapper text);
    public abstract String trimToWidth(String string, int width);
    public final List<String> splitByWidth(String string, int wrapWidth) {
        return this.splitByWidthNative(string, Math.max(wrapWidth, 1));
    }
    public final List<TextWrapper> splitByWidth(TextWrapper text, int wrapWidth) {
        return this.splitByWidthNative(text, Math.max(wrapWidth, 1));
    }
    protected abstract List<String> splitByWidthNative(String string, int wrapWidth);
    protected abstract List<TextWrapper> splitByWidthNative(TextWrapper text, int wrapWidth);
    @Nullable
    public abstract StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft);

    public int getWordWrappedHeight(String text, int maxWidth) {
        return this.getHeight() * this.splitByWidth(text, maxWidth).size();
    }
}
