package com.mndk.bteterrarenderer.mixin.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.gui.IFont;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(value = IFont.class, remap = false)
public class IFontMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static IFont<?,?,?,?> makeDefault() {
        return of(MinecraftClient.getInstance().textRenderer);
    }

    @Unique
    private static IFont<DrawContext, Object, Object, Style> of(TextRenderer textRenderer) { return new IFont<>() {
        public int getHeight() {
            return textRenderer.fontHeight;
        }
        public int getStringWidth(String text) {
            return textRenderer.getWidth(text);
        }
        public int getComponentWidth(Object textComponent) {
            if(textComponent instanceof StringVisitable visitable) {
                textRenderer.getWidth(visitable);
            }
            else if(textComponent instanceof OrderedText text) {
                textRenderer.getWidth(text);
            }
            return 0;
        }
        public int drawStringWithShadow(DrawContext drawContext, String text, float x, float y, int color) {
            return drawContext.drawTextWithShadow(textRenderer, text, (int) x, (int) y, color);
        }
        public int drawComponentWithShadow(DrawContext drawContext, Object textComponent, float x, float y, int color) {
            if(textComponent instanceof Text text) {
                return drawContext.drawTextWithShadow(textRenderer, text, (int) x, (int) y, color);
            }
            else if(textComponent instanceof OrderedText text) {
                return drawContext.drawTextWithShadow(textRenderer, text, (int) x, (int) y, color);
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
        public List<?> splitComponentByWidth(Object textComponent, int wrapWidth) {
            return ChatMessages.breakRenderedChatMessageLines((StringVisitable) textComponent, wrapWidth, textRenderer);
        }
        public Style getStyleComponentFromLine(@Nonnull Object lineComponent, int mouseXFromLeft) {
            if(lineComponent instanceof StringVisitable visitable) {
                return textRenderer.getTextHandler().getStyleAt(visitable, mouseXFromLeft);
            }
            else if(lineComponent instanceof OrderedText text) {
                return textRenderer.getTextHandler().getStyleAt(text, mouseXFromLeft);
            }
            return null;
        }
    };}

}
