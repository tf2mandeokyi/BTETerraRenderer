package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@UtilityClass
@Mixin(value = FontManager.class, remap = false)
public class FontManagerMixin12 {
    @Overwrite
    public int getFontHeight() {
        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    @Overwrite
    public int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    @Overwrite
    public int getWordWrappedHeight(String text, int maxLength) {
        return Minecraft.getMinecraft().fontRenderer.getWordWrappedHeight(text, maxLength);
    }

    @Overwrite
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    @Overwrite
    public void drawSplitString(Object poseStack, String str, int x, int y, int wrapWidth, int textColor) {
        Minecraft.getMinecraft().fontRenderer.drawSplitString(str, x, y, wrapWidth, textColor);
    }

    @Overwrite
    public String trimStringToWidth(String text, int width) {
        return Minecraft.getMinecraft().fontRenderer.trimStringToWidth(text, width);
    }

    @Overwrite
    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(str, wrapWidth);
    }
}