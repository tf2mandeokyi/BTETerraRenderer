package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
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
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return MinecraftClient.getInstance().textRenderer.drawWithShadow((MatrixStack) poseStack, text, x, y, color);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void drawSplitString(Object poseStack, String str, int x, int y, int wrapWidth, int textColor) {
        var textList = MinecraftClient.getInstance().textRenderer.getTextHandler().wrapLines(str, wrapWidth, Style.EMPTY);
        for(var text : textList) {
            MinecraftClient.getInstance().textRenderer.drawWithShadow((MatrixStack) poseStack, text.getString(), x, y, textColor);
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
