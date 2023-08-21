package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class FontManager {
    public int getFontHeight() {
        return MixinUtil.notOverwritten();
    }
    public int getStringWidth(String text) {
        return MixinUtil.notOverwritten(text);
    }
    public int getWordWrappedHeight(String text, int maxLength) {
        return MixinUtil.notOverwritten(text, maxLength);
    }
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return MixinUtil.notOverwritten(poseStack, text, x, y, color);
    }
    public void drawSplitString(Object poseStack, String str, int x, int y, int wrapWidth, int textColor) {
        MixinUtil.notOverwritten(poseStack, str, x, y, wrapWidth, textColor);
    }
    public String trimStringToWidth(String text, int width) {
        return MixinUtil.notOverwritten(text, width);
    }
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return MixinUtil.notOverwritten(str, wrapWidth);
    }

    public void drawCenteredStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        drawStringWithShadow(poseStack, text, x - getStringWidth(text) / 2.0f, y, color);
    }
}
