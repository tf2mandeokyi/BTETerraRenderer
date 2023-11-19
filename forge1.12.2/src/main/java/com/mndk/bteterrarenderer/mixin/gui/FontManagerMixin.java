package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;

@UtilityClass
@Mixin(value = FontManager.class, remap = false)
public class FontManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getFontHeight() {
        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getComponentWidth(Object textComponent) {
        String formatted = ((ITextComponent) textComponent).getFormattedText();
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(formatted);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int drawComponentWithShadow(Object poseStack, Object textComponent, float x, float y, int color) {
        String formatted = ((ITextComponent) textComponent).getFormattedText();
        return Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(formatted, x, y, color);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String trimStringToWidth(String text, int width) {
        return Minecraft.getMinecraft().fontRenderer.trimStringToWidth(text, width);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public List<String> splitStringByWidth(String str, int wrapWidth) {
        return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(str, wrapWidth);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public List<?> splitComponentByWidth(Object textComponent, int wrapWidth) {
        return GuiUtilRenderComponents.splitText((ITextComponent) textComponent, wrapWidth,
                Minecraft.getMinecraft().fontRenderer, true, false);
    }
}