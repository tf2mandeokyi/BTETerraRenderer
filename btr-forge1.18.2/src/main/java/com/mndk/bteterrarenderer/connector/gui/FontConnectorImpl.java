package com.mndk.bteterrarenderer.connector.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

import java.util.List;

public record FontConnectorImpl() implements FontConnector {
    public int getFontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public int getStringWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public int getWordWrappedHeight(String text, int maxLength) {
        return Minecraft.getInstance().font.wordWrapHeight(text, maxLength);
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        return Minecraft.getInstance().font.drawShadow(new PoseStack(), text, x, y, color);
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
//        delegate.drawSplitString(str, x, y, wrapWidth, textColor); // TODO implement this
    }

    public String trimStringToWidth(String text, int width) {
        return Minecraft.getInstance().font.plainSubstrByWidth(text, width);
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
//        return delegate.listFormattedStringToWidth(str, wrapWidth);
        return null;
    }
}
