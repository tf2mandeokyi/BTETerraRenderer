package com.mndk.bteterrarenderer.mcconnector.client.text;

import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import java.util.List;

public class FontWrapperImpl extends AbstractFontWrapper<FontRenderer> {

    public FontWrapperImpl(@Nonnull FontRenderer delegate) {
        super(delegate);
    }

    public int getHeight() {
        return getWrapped().FONT_HEIGHT;
    }
    public int getWidth(String string) {
        return getWrapped().getStringWidth(string);
    }
    public String trimToWidth(String string, int width) {
        return getWrapped().trimStringToWidth(string, width);
    }
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return getWrapped().listFormattedStringToWidth(string, wrapWidth);
    }
}