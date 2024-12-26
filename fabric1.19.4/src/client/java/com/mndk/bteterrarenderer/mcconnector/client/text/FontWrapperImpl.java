package com.mndk.bteterrarenderer.mcconnector.client.text;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

import javax.annotation.Nonnull;
import java.util.List;

public class FontWrapperImpl extends AbstractFontWrapper<TextRenderer> {

    public FontWrapperImpl(@Nonnull TextRenderer delegate) {
        super(delegate);
    }

    public int getHeight() {
        return getWrapped().fontHeight;
    }
    public int getWidth(String string) {
        return getWrapped().getWidth(string);
    }
    public String trimToWidth(String string, int width) {
        return getWrapped().trimToWidth(string, width);
    }
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return getWrapped().getTextHandler().wrapLines(string, wrapWidth, Style.EMPTY)
                .stream().map(StringVisitable::getString).toList();
    }
}
