package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
@SuppressWarnings("UnusedReturnValue")
public class FontManager {
    public int getFontHeight() {
        return MixinUtil.notOverwritten();
    }
    public int getStringWidth(String text) {
        return MixinUtil.notOverwritten(text);
    }
    public int getComponentWidth(Object textComponent) {
        return MixinUtil.notOverwritten(textComponent);
    }
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return MixinUtil.notOverwritten(poseStack, text, x, y, color);
    }
    public int drawComponentWithShadow(Object poseStack, Object textComponent, float x, float y, int color) {
        return MixinUtil.notOverwritten(poseStack, textComponent, x, y, color);
    }
    public String trimStringToWidth(String text, int width) {
        return MixinUtil.notOverwritten(text, width);
    }
    public List<String> splitStringByWidth(String str, int wrapWidth) {
        return MixinUtil.notOverwritten(str, wrapWidth);
    }
    public List<?> splitComponentByWidth(Object textComponent, int wrapWidth) {
        return MixinUtil.notOverwritten(textComponent, wrapWidth);
    }

    public int getWordWrappedHeight(String text, int maxWidth) {
        return getFontHeight() * splitStringByWidth(text, maxWidth).size();
    }
    public void drawSplitString(Object poseStack, String str, int x, int y, int wrapWidth, int textColor) {
        List<String> splitLines = splitStringByWidth(str, wrapWidth);
        for(String line : splitLines) {
            drawStringWithShadow(poseStack, line, x, y, textColor);
            y += getFontHeight();
        }
    }
    public void drawStringWithShadow(Object poseStack, String text, TextAlign align, float x, float y, float width, int color) {
        if(align == TextAlign.LEFT) {
            FontManager.drawStringWithShadow(poseStack, text, x, y, color);
        }
        else if(align == TextAlign.RIGHT) {
            FontManager.drawStringWithShadow(poseStack, text, x + width - FontManager.getStringWidth(text), y, color);
        }
        else if(align == TextAlign.CENTER) {
            FontManager.drawCenteredStringWithShadow(poseStack, text, x + width / 2f, y, color);
        }
    }
    public void drawComponentWithShadow(Object poseStack, Object textComponent, TextAlign align, float x, float y, float width, int color) {
        if(align == TextAlign.LEFT) {
            FontManager.drawComponentWithShadow(poseStack, textComponent, x, y, color);
        }
        else if(align == TextAlign.RIGHT) {
            FontManager.drawComponentWithShadow(poseStack, textComponent, x + width - FontManager.getComponentWidth(textComponent), y, color);
        }
        else if(align == TextAlign.CENTER) {
            FontManager.drawCenteredComponentWithShadow(poseStack, textComponent, x + width / 2f, y, color);
        }
    }
    public void drawCenteredStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        drawStringWithShadow(poseStack, text, x - getStringWidth(text) / 2.0f, y, color);
    }
    public void drawCenteredComponentWithShadow(Object poseStack, Object textComponent, float x, float y, int color) {
        drawComponentWithShadow(poseStack, textComponent, x - getComponentWidth(textComponent) / 2.0f, y, color);
    }
}
