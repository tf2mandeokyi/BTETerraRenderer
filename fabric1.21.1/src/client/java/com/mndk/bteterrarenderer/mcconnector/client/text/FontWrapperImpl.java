package com.mndk.bteterrarenderer.mcconnector.client.text;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class FontWrapperImpl extends AbstractFontWrapper {

    @Nonnull public final TextRenderer delegate;

    public int getHeight() {
        return delegate.fontHeight;
    }
    public int getWidth(String string) {
        return delegate.getWidth(string);
    }
    public String trimToWidth(String string, int width) {
        return delegate.trimToWidth(string, width);
    }
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return delegate.getTextHandler().wrapLines(string, wrapWidth, Style.EMPTY)
                .stream().map(StringVisitable::getString).toList();
    }
}
