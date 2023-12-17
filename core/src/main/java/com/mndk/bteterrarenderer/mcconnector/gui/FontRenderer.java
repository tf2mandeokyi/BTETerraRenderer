package com.mndk.bteterrarenderer.mcconnector.gui;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.mcconnector.MixinUtil;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public abstract class FontRenderer<PoseStack, TextComponent, LineComponent, StyleComponent> {

    public static final FontRenderer<Object, Object, Object, Object> DEFAULT = BTRUtil.uncheckedCast(makeDefault());
    private static FontRenderer<?, ?, ?, ?> makeDefault() {
        return MixinUtil.notOverwritten();
    }

    public abstract int getHeight();
    public abstract int getStringWidth(String text);
    public abstract int getComponentWidth(TextComponent textComponent);
    public abstract int drawStringWithShadow(PoseStack poseStack, String text, float x, float y, int color);
    public abstract int drawComponentWithShadow(PoseStack poseStack, TextComponent textComponent, float x, float y, int color);
    public abstract String trimStringToWidth(String text, int width);
    public abstract List<String> splitStringByWidth(String str, int wrapWidth);
    public abstract List<?> splitComponentByWidth(TextComponent textComponent, int wrapWidth);
    public abstract StyleComponent getStyleComponentFromLine(@Nonnull LineComponent lineComponent, int mouseXFromLeft);

    public int getWordWrappedHeight(String text, int maxWidth) {
        return this.getHeight() * this.splitStringByWidth(text, maxWidth).size();
    }
    public void drawSplitString(PoseStack poseStack, String str, int x, int y, int wrapWidth, int textColor) {
        List<String> splitLines = this.splitStringByWidth(str, wrapWidth);
        for(String line : splitLines) {
            this.drawStringWithShadow(poseStack, line, x, y, textColor);
            y += this.getHeight();
        }
    }
    public void drawStringWithShadow(PoseStack poseStack, String text, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawStringWithShadow(poseStack, text, x, y, color); break;
            case RIGHT:  this.drawStringWithShadow(poseStack, text, x + width - this.getStringWidth(text), y, color); break;
            case CENTER: this.drawCenteredStringWithShadow(poseStack, text, x + width / 2f, y, color); break;
        }
    }
    public void drawStringWithShadow(PoseStack poseStack, String text, HorizontalAlign hAlign, VerticalAlign vAlign, float x, float y, int color) {
        String[] lines = text.split("\n");

        float left, top;
        int width = this.getStringWidth(text), height = this.getHeight() * lines.length;
        switch(hAlign) {
            case LEFT:   left = x; break;
            case CENTER: left = x - (float) width / 2; break;
            case RIGHT:  left = x - width; break;
            default:     throw new RuntimeException("Unknown align value");
        }
        switch(vAlign) {
            case TOP:    top = y; break;
            case MIDDLE: top = y - (float) height / 2; break;
            case BOTTOM: top = y - height; break;
            default:     throw new RuntimeException("Unknown align value");
        }
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            this.drawStringWithShadow(poseStack, line, left, top + i * this.getHeight(), color);
        }
    }
    public void drawComponentWithShadow(PoseStack poseStack, TextComponent textComponent, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawComponentWithShadow(poseStack, textComponent, x, y, color); break;
            case RIGHT:  this.drawComponentWithShadow(poseStack, textComponent, x + width - this.getComponentWidth(textComponent), y, color); break;
            case CENTER: this.drawCenteredComponentWithShadow(poseStack, textComponent, x + width / 2f, y, color); break;
        }
    }
    public void drawCenteredStringWithShadow(PoseStack poseStack, String text, float x, float y, int color) {
        this.drawStringWithShadow(poseStack, text, x - getStringWidth(text) / 2.0f, y, color);
    }
    public void drawCenteredComponentWithShadow(PoseStack poseStack, TextComponent textComponent, float x, float y, int color) {
        this.drawComponentWithShadow(poseStack, textComponent, x - getComponentWidth(textComponent) / 2.0f, y, color);
    }
}
