package com.mndk.bteterrarenderer.connector.minecraft.gui;

import java.util.List;

public interface FontRendererConnector {
    int getFontHeight();
    int getStringWidth(String text);
    int getWordWrappedHeight(String text, int maxLength);
    void drawStringWithShadow(String text, float x, float y, int color);
    void drawSplitString(String str, int x, int y, int wrapWidth, int textColor);
    default void drawCenteredStringWithShadow(String text, int x, int y, int color) {
        this.drawStringWithShadow(text, (float) (x - this.getStringWidth(text) / 2), (float) y, color);
    }
    String trimStringToWidth(String text, int width);
    List<String> listFormattedStringToWidth(String str, int wrapWidth);
}
