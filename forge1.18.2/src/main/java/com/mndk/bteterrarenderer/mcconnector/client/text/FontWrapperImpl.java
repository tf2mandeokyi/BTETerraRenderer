package com.mndk.bteterrarenderer.mcconnector.client.text;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;
import java.util.List;

public class FontWrapperImpl extends AbstractFontWrapper<Font> {

    public FontWrapperImpl(@Nonnull Font delegate) {
        super(delegate);
    }

    public int getHeight() {
        return getWrapped().lineHeight;
    }
    public int getWidth(String string) {
        return getWrapped().width(string);
    }
    public String trimToWidth(String string, int width) {
        return getWrapped().plainSubstrByWidth(string, width);
    }
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return getWrapped().getSplitter().splitLines(string, wrapWidth, Style.EMPTY)
                .stream().map(FormattedText::getString).toList();
    }
}
