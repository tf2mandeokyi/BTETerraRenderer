package com.mndk.bteterrarenderer.connector.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;

import java.util.List;

public record IFontRendererImpl(Font delegate) implements IFontRenderer {
    public int getFontHeight() {
        return delegate.lineHeight;
    }

    public int getStringWidth(String text) {
        return delegate.width(text);
    }

    public int getWordWrappedHeight(String text, int maxLength) {
        return delegate.wordWrapHeight(text, maxLength);
    }

    public void drawStringWithShadow(String text, float x, float y, int color) {
        delegate.drawShadow(new PoseStack(), text, x, y, color);
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
//        delegate.drawSplitString(str, x, y, wrapWidth, textColor); // TODO implement this
    }

    public String trimStringToWidth(String text, int width) {
//        return delegate.trimStringToWidth(text, width);
        return null;
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
//        return delegate.listFormattedStringToWidth(str, wrapWidth);
        return null;
    }
}
