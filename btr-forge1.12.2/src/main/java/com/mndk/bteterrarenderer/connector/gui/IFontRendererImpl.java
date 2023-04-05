package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.gui.IFontRenderer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.FontRenderer;

import java.util.List;

@RequiredArgsConstructor @Getter
public class IFontRendererImpl implements IFontRenderer {
    private final FontRenderer fontRenderer;

    public int getFontHeight() { return fontRenderer.FONT_HEIGHT; }
    public int getStringWidth(String text) { return fontRenderer.getStringWidth(text); }
    public int getWordWrappedHeight(String text, int maxLength) {
        return fontRenderer.getWordWrappedHeight(text, maxLength);
    }
    public int drawStringWithShadow(String text, float x, float y, int color) {
        return fontRenderer.drawStringWithShadow(text, x, y, color);
    }
    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        fontRenderer.drawSplitString(str, x, y, wrapWidth, textColor);
    }
    public String trimStringToWidth(String text, int width) {
        return fontRenderer.trimStringToWidth(text, width);
    }
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return fontRenderer.listFormattedStringToWidth(str, wrapWidth);
    }
}
