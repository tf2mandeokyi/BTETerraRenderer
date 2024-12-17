package com.mndk.bteterrarenderer.mcconnector.client.text;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FontWrapperImpl extends FontWrapper<Font> {

    public FontWrapperImpl(@Nonnull Font delegate) {
        super(delegate);
    }

    public int getHeight() {
        return getThisWrapped().lineHeight;
    }
    public int getWidth(String string) {
        return getThisWrapped().width(string);
    }
    public int getWidth(TextWrapper textWrapper) {
        Object textComponent = textWrapper.get();
        if (textComponent instanceof FormattedText text) {
            return getThisWrapped().width(text);
        }
        else if (textComponent instanceof FormattedCharSequence sequence) {
            return getThisWrapped().width(sequence);
        }
        return 0;
    }
    public String trimToWidth(String string, int width) {
        return getThisWrapped().plainSubstrByWidth(string, width);
    }
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return getThisWrapped().getSplitter().splitLines(string, wrapWidth, Style.EMPTY)
                .stream().map(FormattedText::getString).toList();
    }
    protected List<TextWrapper> splitByWidthUnsafe(TextWrapper text, int wrapWidth) {
        Object textComponent = text.get();
        return ComponentRenderUtils.wrapComponents((FormattedText) textComponent, wrapWidth, getThisWrapped())
                .stream().map(TextWrapper::new).toList();
    }
    @Nullable
    public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
        Object lineComponent = textWrapper.get();
        Style style = null;
        if (lineComponent instanceof FormattedText text) {
            style = getThisWrapped().getSplitter().componentStyleAtWidth(text, mouseXFromLeft);
        }
        else if (lineComponent instanceof FormattedCharSequence sequence) {
            style = getThisWrapped().getSplitter().componentStyleAtWidth(sequence, mouseXFromLeft);
        }
        return style != null ? new StyleWrapper(style) : null;
    }
}
