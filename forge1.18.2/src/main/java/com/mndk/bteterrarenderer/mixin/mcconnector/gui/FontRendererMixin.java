package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
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
import javax.annotation.Nullable;
import java.util.List;

@UtilityClass
@Mixin(value = FontRenderer.class, remap = false)
public class FontRendererMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static FontRenderer makeDefault() {
        return bTETerraRenderer$of(Minecraft.getInstance().font);
    }

    @Unique
    private static FontRenderer bTETerraRenderer$of(Font font) { return new FontRenderer() {
        public int getHeight() {
            return font.lineHeight;
        }
        public int getStringWidth(String text) {
            return font.width(text);
        }
        public int getComponentWidth(TextWrapper textWrapper) {
            Object textComponent = textWrapper.get();
            if(textComponent instanceof FormattedText text) {
                return font.width(text);
            }
            else if(textComponent instanceof FormattedCharSequence sequence) {
                return font.width(sequence);
            }
            return 0;
        }
        public int drawStringWithShadow(DrawContextWrapper drawContextWrapper, String text, float x, float y, int color) {
            PoseStack poseStack = drawContextWrapper.get();
            return font.drawShadow(poseStack, text, x, y, color);
        }
        public int drawComponentWithShadow(DrawContextWrapper drawContextWrapper, TextWrapper textWrapper, float x, float y, int color) {
            PoseStack poseStack = drawContextWrapper.get();
            Object textComponent = textWrapper.get();
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
        public List<TextWrapper> splitComponentByWidth(TextWrapper textWrapper, int wrapWidth) {
            Object textComponent = textWrapper.get();
            return ComponentRenderUtils.wrapComponents((FormattedText) textComponent, wrapWidth, font)
                    .stream().map(TextWrapper::new).toList();
        }
        @Nullable
        public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
            Object lineComponent = textWrapper.get();
            Style style = null;
            if(lineComponent instanceof FormattedText text) {
                style = font.getSplitter().componentStyleAtWidth(text, mouseXFromLeft);
            }
            else if(lineComponent instanceof FormattedCharSequence sequence) {
                style = font.getSplitter().componentStyleAtWidth(sequence, mouseXFromLeft);
            }
            return style != null ? new StyleWrapper(style) : null;
        }
    };}

}
