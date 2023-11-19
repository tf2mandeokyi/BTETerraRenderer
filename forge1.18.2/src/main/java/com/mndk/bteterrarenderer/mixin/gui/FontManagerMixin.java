package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.FontManager;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
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
        return Minecraft.getInstance().font.lineHeight;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getStringWidth(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int getComponentWidth(Object textComponent) {
        if(textComponent instanceof FormattedText text) {
            return Minecraft.getInstance().font.width(text);
        }
        else if(textComponent instanceof FormattedCharSequence sequence) {
            return Minecraft.getInstance().font.width(sequence);
        }
        return 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
        return Minecraft.getInstance().font.drawShadow((PoseStack) poseStack, text, x, y, color);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public int drawComponentWithShadow(Object poseStack, Object textComponent, float x, float y, int color) {
        if(textComponent instanceof Component component) {
            return Minecraft.getInstance().font.drawShadow((PoseStack) poseStack, component, x, y, color);
        }
        else if(textComponent instanceof FormattedCharSequence sequence) {
            return Minecraft.getInstance().font.drawShadow((PoseStack) poseStack, sequence, x, y, color);
        }
        return 0;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public String trimStringToWidth(String text, int width) {
        return Minecraft.getInstance().font.plainSubstrByWidth(text, width);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public List<String> splitStringByWidth(String str, int wrapWidth) {
        return Minecraft.getInstance().font.getSplitter().splitLines(str, wrapWidth, Style.EMPTY)
                .stream().map(FormattedText::getString).toList();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public List<?> splitComponentByWidth(Object textComponent, int wrapWidth) {
        return ComponentRenderUtils.wrapComponents((FormattedText) textComponent, wrapWidth, Minecraft.getInstance().font);
    }
}
