package com.mndk.bteterrarenderer.mcconnector.client.text;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class FontWrapperImpl extends AbstractFontWrapper {

    @Nonnull public final FontRenderer delegate;

    public int getHeight() {
        return delegate.FONT_HEIGHT;
    }
    public int getWidth(String string) {
        return delegate.getStringWidth(string);
    }
    public String trimToWidth(String string, int width) {
        return delegate.trimStringToWidth(string, width);
    }
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return delegate.listFormattedStringToWidth(string, wrapWidth);
    }
}