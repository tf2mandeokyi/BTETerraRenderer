package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.FontRenderer;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = FontRenderer.class, remap = false)
public class FontRendererMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static FontRenderer makeDefault() {
        return of(MinecraftClient.getInstance().textRenderer);
    }

    @Unique
    private static FontRenderer of(TextRenderer textRenderer) { return new FontRenderer() {
        public int getHeight() {
            return textRenderer.fontHeight;
        }
        public int getStringWidth(String text) {
            return textRenderer.getWidth(text);
        }
        public int getComponentWidth(TextWrapper textWrapper) {
            Object textComponent = textWrapper.get();
            if(textComponent instanceof StringVisitable visitable) {
                return textRenderer.getWidth(visitable);
            }
            else if(textComponent instanceof OrderedText text) {
                return textRenderer.getWidth(text);
            }
            return 0;
        }
        public int drawStringWithShadow(DrawContextWrapper drawContextWrapper, String text, float x, float y, int color) {
            MatrixStack poseStack = drawContextWrapper.get();
            return textRenderer.drawWithShadow(poseStack, text, x, y, color);
        }
        public int drawComponentWithShadow(DrawContextWrapper drawContextWrapper, TextWrapper textWrapper, float x, float y, int color) {
            MatrixStack poseStack = drawContextWrapper.get();
            Object textComponent = textWrapper.get();
            if(textComponent instanceof Text text) {
                return textRenderer.drawWithShadow(poseStack, text, x, y, color);
            }
            else if(textComponent instanceof OrderedText text) {
                return textRenderer.drawWithShadow(poseStack, text, x, y, color);
            }
            return 0;
        }
        public String trimStringToWidth(String text, int width) {
            return textRenderer.trimToWidth(text, width);
        }
        public List<String> splitStringByWidth(String str, int wrapWidth) {
            return textRenderer.getTextHandler().wrapLines(str, wrapWidth, Style.EMPTY)
                    .stream().map(StringVisitable::getString).toList();
        }
        public List<TextWrapper> splitComponentByWidth(TextWrapper textWrapper, int wrapWidth) {
            Object textComponent = textWrapper.get();
            return ChatMessages.breakRenderedChatMessageLines((StringVisitable) textComponent, wrapWidth, textRenderer)
                    .stream().map(TextWrapper::new).toList();
        }
        @Nullable
        public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
            Object lineComponent = textWrapper.get();
            Style style = null;
            if(lineComponent instanceof StringVisitable visitable) {
                style = textRenderer.getTextHandler().getStyleAt(visitable, mouseXFromLeft);
            }
            else if(lineComponent instanceof OrderedText text) {
                style = textRenderer.getTextHandler().getStyleAt(text, mouseXFromLeft);
            }
            return style != null ? new StyleWrapper(style) : null;
        }
    };}

}
