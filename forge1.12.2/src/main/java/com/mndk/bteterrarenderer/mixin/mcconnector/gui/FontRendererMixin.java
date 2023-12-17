package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;
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
        return bTETerraRenderer$of(Minecraft.getMinecraft().fontRenderer);
    }

    @Unique
    private static FontRenderer<?,?,?,?> bTETerraRenderer$of(net.minecraft.client.gui.FontRenderer fontRenderer) { return new FontRenderer<Object, ITextComponent, ITextComponent, ITextComponent>() {
        public int getHeight() {
            return fontRenderer.FONT_HEIGHT;
        }
        public int getStringWidth(String text) {
            return fontRenderer.getStringWidth(text);
        }
        public int getComponentWidth(ITextComponent textComponent) {
            String formattedText = textComponent.getFormattedText();
            return fontRenderer.getStringWidth(formattedText);
        }
        public int drawStringWithShadow(Object poseStack, String text, float x, float y, int color) {
            return fontRenderer.drawStringWithShadow(text, x, y, color);
        }
        public int drawComponentWithShadow(Object poseStack, ITextComponent textComponent, float x, float y, int color) {
            String formatted = textComponent.getFormattedText();
            return fontRenderer.drawStringWithShadow(formatted, x, y, color);
        }
        public String trimStringToWidth(String text, int width) {
            return fontRenderer.trimStringToWidth(text, width);
        }
        public List<String> splitStringByWidth(String str, int wrapWidth) {
            return fontRenderer.listFormattedStringToWidth(str, wrapWidth);
        }
        public List<?> splitComponentByWidth(ITextComponent textComponent, int wrapWidth) {
            return GuiUtilRenderComponents.splitText(textComponent, wrapWidth, fontRenderer, true, false);
        }
        public ITextComponent getStyleComponentFromLine(@Nonnull ITextComponent lineComponent, int mouseXFromLeft) {
            int xPos = 0;
            ITextComponent clicked = null;
            for(ITextComponent child : lineComponent.getSiblings()) {
                int childWidth = FontRenderer.DEFAULT.getComponentWidth(child);
                if(xPos <= mouseXFromLeft && mouseXFromLeft <= xPos + childWidth) {
                    clicked = child; break;
                }
                xPos += childWidth;
            }
            return clicked;
        }
    };}

}