package com.mndk.bteterrarenderer.connector.gui;

import com.mndk.bteterrarenderer.connector.ImplFinder;

import java.util.List;

public interface FontConnector {
    FontConnector INSTANCE = ImplFinder.search();

    int getFontHeight();
    int getStringWidth(String text);
    int getWordWrappedHeight(String text, int maxLength);
    int drawStringWithShadow(Object poseStack, String text, float x, float y, int color);
    void drawSplitString(Object poseStack, String str, int x, int y, int wrapWidth, int textColor);
    default void drawCenteredStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        this.drawStringWithShadow(poseStack, text, x - this.getStringWidth(text) / 2.0f, y, color);
    }
    String trimStringToWidth(String text, int width);
    List<String> listFormattedStringToWidth(String str, int wrapWidth);
}
