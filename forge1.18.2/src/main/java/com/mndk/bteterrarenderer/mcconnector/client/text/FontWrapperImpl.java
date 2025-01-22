package com.mndk.bteterrarenderer.mcconnector.client.text;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class FontWrapperImpl extends AbstractFontWrapper {

    @Nonnull public final Font delegate;

    public int getHeight() {
        return delegate.lineHeight;
    }
    public int getWidth(String string) {
        return delegate.width(string);
    }
    public String trimToWidth(String string, int width) {
        return delegate.plainSubstrByWidth(string, width);
    }
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return delegate.getSplitter().splitLines(string, wrapWidth, Style.EMPTY)
                .stream().map(FormattedText::getString).toList();
    }
}
