package com.mndk.bteterrarenderer.connector.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public record FontConnectorImpl18() implements FontConnector {
    public int getFontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    public int getStringWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public int getWordWrappedHeight(String text, int maxLength) {
        return Minecraft.getInstance().font.wordWrapHeight(text, maxLength);
    }

    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return Minecraft.getInstance().font.drawShadow((PoseStack) poseStack, text, x, y, color);
    }

    public void drawSplitString(Object poseStack, String str, int x, int y, int wrapWidth, int textColor) {
        var textList = Minecraft.getInstance().font.getSplitter().splitLines(str, wrapWidth, Style.EMPTY);
        for(var text : textList) {
            Minecraft.getInstance().font.drawShadow((PoseStack) poseStack, text.getString(), x, y, textColor);
            y += Minecraft.getInstance().font.lineHeight;
        }
    }

    public String trimStringToWidth(String text, int width) {
        return Minecraft.getInstance().font.plainSubstrByWidth(text, width);
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        var textList = Minecraft.getInstance().font.getSplitter().splitLines(str, wrapWidth, Style.EMPTY);
        List<String> result = new ArrayList<>(textList.size());
        for(var text : textList) {
            result.add(text.getString());
        }
        return result;
    }
}
