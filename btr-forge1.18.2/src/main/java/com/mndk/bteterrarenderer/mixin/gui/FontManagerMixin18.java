package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Mixin(value = FontManager.class, remap = false)
public class FontManagerMixin18 {
    @Overwrite
    public int getFontHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }

    @Overwrite
    public int getStringWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    @Overwrite
    public int getWordWrappedHeight(String text, int maxLength) {
        return Minecraft.getInstance().font.wordWrapHeight(text, maxLength);
    }

    @Overwrite
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return Minecraft.getInstance().font.drawShadow((PoseStack) poseStack, text, x, y, color);
    }

    @Overwrite
    public void drawSplitString(Object poseStack, String str, int x, int y, int wrapWidth, int textColor) {
        var textList = Minecraft.getInstance().font.getSplitter().splitLines(str, wrapWidth, Style.EMPTY);
        for(var text : textList) {
            Minecraft.getInstance().font.drawShadow((PoseStack) poseStack, text.getString(), x, y, textColor);
            y += Minecraft.getInstance().font.lineHeight;
        }
    }

    @Overwrite
    public String trimStringToWidth(String text, int width) {
        return Minecraft.getInstance().font.plainSubstrByWidth(text, width);
    }

    @Overwrite
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        var textList = Minecraft.getInstance().font.getSplitter().splitLines(str, wrapWidth, Style.EMPTY);
        List<String> result = new ArrayList<>(textList.size());
        for(var text : textList) {
            result.add(text.getString());
        }
        return result;
    }
}
