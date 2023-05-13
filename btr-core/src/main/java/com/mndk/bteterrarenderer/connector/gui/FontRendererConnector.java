package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ImplFinder;

import java.util.List;

public interface FontRendererConnector {
    FontRendererConnector INSTANCE = ImplFinder.search();

    int getFontHeight();
    int getStringWidth(String text);
    int getWordWrappedHeight(String text, int maxLength);
    void drawStringWithShadow(String text, float x, float y, int color);
    void drawSplitString(String str, int x, int y, int wrapWidth, int textColor);
    default void drawCenteredStringWithShadow(String text, float x, float y, int color) {
        this.drawStringWithShadow(text, x - this.getStringWidth(text) / 2.0f, y, color);
    }
    String trimStringToWidth(String text, int width);
    List<String> listFormattedStringToWidth(String str, int wrapWidth);
}
