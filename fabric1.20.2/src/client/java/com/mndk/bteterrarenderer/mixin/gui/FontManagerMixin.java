package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Mixin(value = FontManager.class, remap = false)
public class FontManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getFontHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getStringWidth(String text) {
        return MinecraftClient.getInstance().textRenderer.getWidth(text);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getWordWrappedHeight(String text, int maxLength) {
        return MinecraftClient.getInstance().textRenderer.getWrappedLinesHeight(text, maxLength);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int drawStringWithShadow(Object drawContext, String text, float x, float y, int color) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        return ((DrawContext) drawContext).drawTextWithShadow(textRenderer, text, (int) x, (int) y, color);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawSplitString(Object drawContext, String str, int x, int y, int wrapWidth, int textColor) {
        var textList = MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(str, wrapWidth, Style.EMPTY);
        for(var text : textList) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            ((DrawContext) drawContext).drawTextWithShadow(textRenderer, text.getString(), x, y, textColor);
            y += MinecraftClient.getInstance().textRenderer.fontHeight;
        }
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String trimStringToWidth(String text, int width) {
        return MinecraftClient.getInstance().textRenderer.trimToWidth(text, width);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        var textList = MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(str, wrapWidth, Style.EMPTY);
        List<String> result = new ArrayList<>(textList.size());
        for(var text : textList) {
            result.add(text.getString());
        }
        return result;
    }

}
