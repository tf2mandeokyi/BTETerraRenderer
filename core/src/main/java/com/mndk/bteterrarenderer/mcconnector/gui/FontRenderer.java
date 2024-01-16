package com.mndk.bteterrarenderer.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public abstract class FontRenderer {

    public static final FontRenderer DEFAULT = makeDefault();
    private static FontRenderer makeDefault() {
        return MixinUtil.notOverwritten();
    }

    public abstract int getHeight();
    public abstract int getStringWidth(String text);
    public abstract int getComponentWidth(TextWrapper textComponent);
    public abstract int drawStringWithShadow(DrawContextWrapper drawContextWrapper, String text, float x, float y, int color);
    public abstract int drawComponentWithShadow(DrawContextWrapper drawContextWrapper, TextWrapper textComponent, float x, float y, int color);
    public abstract String trimStringToWidth(String text, int width);
    public abstract List<String> splitStringByWidth(String str, int wrapWidth);
    public abstract List<TextWrapper> splitComponentByWidth(TextWrapper textComponent, int wrapWidth);
    @Nullable
    public abstract StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper lineComponent, int mouseXFromLeft);

    public int getWordWrappedHeight(String text, int maxWidth) {
        return this.getHeight() * this.splitStringByWidth(text, maxWidth).size();
    }
    public void drawSplitString(DrawContextWrapper drawContextWrapper, String str, int x, int y, int wrapWidth, int textColor) {
        List<String> splitLines = this.splitStringByWidth(str, wrapWidth);
        for(String line : splitLines) {
            this.drawStringWithShadow(drawContextWrapper, line, x, y, textColor);
            y += this.getHeight();
        }
    }
    public void drawStringWithShadow(DrawContextWrapper drawContextWrapper, String text, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawStringWithShadow(drawContextWrapper, text, x, y, color); break;
            case RIGHT:  this.drawStringWithShadow(drawContextWrapper, text, x + width - this.getStringWidth(text), y, color); break;
            case CENTER: this.drawCenteredStringWithShadow(drawContextWrapper, text, x + width / 2f, y, color); break;
        }
    }
    public void drawStringWithShadow(DrawContextWrapper drawContextWrapper, String text, HorizontalAlign hAlign, VerticalAlign vAlign, float x, float y, int color) {
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
            this.drawStringWithShadow(drawContextWrapper, line, left, top + i * this.getHeight(), color);
        }
    }
    public void drawComponentWithShadow(DrawContextWrapper drawContextWrapper, TextWrapper textComponent, HorizontalAlign align, float x, float y, float width, int color) {
        switch (align) {
            case LEFT:   this.drawComponentWithShadow(drawContextWrapper, textComponent, x, y, color); break;
            case RIGHT:  this.drawComponentWithShadow(drawContextWrapper, textComponent, x + width - this.getComponentWidth(textComponent), y, color); break;
            case CENTER: this.drawCenteredComponentWithShadow(drawContextWrapper, textComponent, x + width / 2f, y, color); break;
        }
    }
    public void drawCenteredStringWithShadow(DrawContextWrapper drawContextWrapper, String text, float x, float y, int color) {
        this.drawStringWithShadow(drawContextWrapper, text, x - getStringWidth(text) / 2.0f, y, color);
    }
    public void drawCenteredComponentWithShadow(DrawContextWrapper drawContextWrapper, TextWrapper textComponent, float x, float y, int color) {
        this.drawComponentWithShadow(drawContextWrapper, textComponent, x - getComponentWidth(textComponent) / 2.0f, y, color);
    }
}
