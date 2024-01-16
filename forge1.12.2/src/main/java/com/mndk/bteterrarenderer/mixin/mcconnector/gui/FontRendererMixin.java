package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
@Mixin(value = FontRenderer.class, remap = false)
public class FontRendererMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static FontRenderer makeDefault() {
        return bTETerraRenderer$of(Minecraft.getMinecraft().fontRenderer);
    }

    @Unique
    private static FontRenderer bTETerraRenderer$of(net.minecraft.client.gui.FontRenderer fontRenderer) { return new FontRenderer() {
        public int getHeight() {
            return fontRenderer.FONT_HEIGHT;
        }
        public int getStringWidth(String text) {
            return fontRenderer.getStringWidth(text);
        }
        public int getComponentWidth(TextWrapper textWrapper) {
            ITextComponent textComponent = textWrapper.get();
            String formattedText = textComponent.getFormattedText();
            return fontRenderer.getStringWidth(formattedText);
        }
        public int drawStringWithShadow(DrawContextWrapper drawContextWrapper, String text, float x, float y, int color) {
            return fontRenderer.drawStringWithShadow(text, x, y, color);
        }
        public int drawComponentWithShadow(DrawContextWrapper drawContextWrapper, TextWrapper textWrapper, float x, float y, int color) {
            ITextComponent textComponent = textWrapper.get();
            String formatted = textComponent.getFormattedText();
            return fontRenderer.drawStringWithShadow(formatted, x, y, color);
        }
        public String trimStringToWidth(String text, int width) {
            return fontRenderer.trimStringToWidth(text, width);
        }
        public List<String> splitStringByWidth(String str, int wrapWidth) {
            return fontRenderer.listFormattedStringToWidth(str, wrapWidth);
        }
        public List<TextWrapper> splitComponentByWidth(TextWrapper textWrapper, int wrapWidth) {
            ITextComponent textComponent = textWrapper.get();
            return GuiUtilRenderComponents.splitText(textComponent, wrapWidth, fontRenderer, true, false)
                    .stream().map(TextWrapper::new).collect(Collectors.toList());
        }
        @Nullable
        public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper lineComponent, int mouseXFromLeft) {
            int xPos = 0;
            ITextComponent clicked = null, textComponent = lineComponent.get();
            for(ITextComponent child : textComponent.getSiblings()) {
                int childWidth = FontRenderer.DEFAULT.getComponentWidth(new TextWrapper(child));
                if(xPos <= mouseXFromLeft && mouseXFromLeft <= xPos + childWidth) {
                    clicked = child; break;
                }
                xPos += childWidth;
            }
            return clicked != null ? new StyleWrapper(clicked) : null;
        }
    };}

}