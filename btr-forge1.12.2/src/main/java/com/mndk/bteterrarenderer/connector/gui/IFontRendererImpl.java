package com.mndk.bteterrarenderer.connector.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.FontRenderer;

import java.util.List;

@RequiredArgsConstructor @Getter
public class IFontRendererImpl implements IFontRenderer {
    private final FontRenderer delegate;

    public int getFontHeight() { return delegate.FONT_HEIGHT; }
    public int getStringWidth(String text) { return delegate.getStringWidth(text); }
    public int getWordWrappedHeight(String text, int maxLength) {
        return delegate.getWordWrappedHeight(text, maxLength);
    }
    public void drawStringWithShadow(String text, float x, float y, int color) {
        delegate.drawStringWithShadow(text, x, y, color);
    }
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        delegate.drawSplitString(str, x, y, wrapWidth, textColor);
    }
    public String trimStringToWidth(String text, int width) {
        return delegate.trimStringToWidth(text, width);
    }
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return delegate.listFormattedStringToWidth(str, wrapWidth);
    }
}
