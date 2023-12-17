package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import java.util.List;

@UtilityClass
@Mixin(value = FontRenderer.class, remap = false)
public class FontRendererMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static FontRenderer<?,?,?,?> makeDefault() {
        return bTETerraRenderer$of(Minecraft.getInstance().font);
    }

    @Unique
    private static FontRenderer<PoseStack, Object, Object, Style> bTETerraRenderer$of(Font font) { return new FontRenderer<>() {
        public int getHeight() {
            return font.lineHeight;
        }
        public int getStringWidth(String text) {
            return font.width(text);
        }
        public int getComponentWidth(Object textComponent) {
            if(textComponent instanceof FormattedText text) {
                return font.width(text);
            }
            else if(textComponent instanceof FormattedCharSequence sequence) {
                return font.width(sequence);
            }
            return 0;
        }
        public int drawStringWithShadow(PoseStack poseStack, String text, float x, float y, int color) {
            return font.drawShadow(poseStack, text, x, y, color);
        }
        public int drawComponentWithShadow(PoseStack poseStack, Object textComponent, float x, float y, int color) {
            if(textComponent instanceof Component component) {
                return font.drawShadow(poseStack, component, x, y, color);
            }
            else if(textComponent instanceof FormattedCharSequence sequence) {
                return font.drawShadow(poseStack, sequence, x, y, color);
            }
            return 0;
        }
        public String trimStringToWidth(String text, int width) {
            return font.plainSubstrByWidth(text, width);
        }
        public List<String> splitStringByWidth(String str, int wrapWidth) {
            return font.getSplitter().splitLines(str, wrapWidth, Style.EMPTY)
                    .stream().map(FormattedText::getString).toList();
        }
        public List<?> splitComponentByWidth(Object textComponent, int wrapWidth) {
            return ComponentRenderUtils.wrapComponents((FormattedText) textComponent, wrapWidth, font);
        }
        public Style getStyleComponentFromLine(@Nonnull Object lineComponent, int mouseXFromLeft) {
            if(lineComponent instanceof FormattedText text) {
                return font.getSplitter().componentStyleAtWidth(text, mouseXFromLeft);
            }
            else if(lineComponent instanceof FormattedCharSequence sequence) {
                return font.getSplitter().componentStyleAtWidth(sequence, mouseXFromLeft);
            }
            return null;
        }
    };}

}
