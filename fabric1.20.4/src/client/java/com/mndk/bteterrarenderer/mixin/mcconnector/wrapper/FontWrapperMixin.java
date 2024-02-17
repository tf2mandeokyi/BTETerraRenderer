package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.wrapper.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Mixin(value = FontWrapper.class, remap = false)
public class FontWrapperMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static FontWrapper<?> makeDefault() {
        return of(MinecraftClient.getInstance().textRenderer);
    }

    @Unique
    private static FontWrapper<?> of(TextRenderer textRenderer) { return new FontWrapper<TextRenderer>(textRenderer) {
        public int getHeight() {
            return getThisWrapped().fontHeight;
        }
        public int getWidth(String string) {
            return getThisWrapped().getWidth(string);
        }
        public int getWidth(TextWrapper textWrapper) {
            Object textComponent = textWrapper.get();
            if(textComponent instanceof StringVisitable visitable) {
                return getThisWrapped().getWidth(visitable);
            }
            else if(textComponent instanceof OrderedText text) {
                return getThisWrapped().getWidth(text);
            }
            return 0;
        }
        public String trimToWidth(String string, int width) {
            return getThisWrapped().trimToWidth(string, width);
        }
        protected List<String> splitByWidthNative(String string, int wrapWidth) {
            return getThisWrapped().getTextHandler().wrapLines(string, wrapWidth, Style.EMPTY)
                    .stream().map(StringVisitable::getString).toList();
        }
        protected List<TextWrapper> splitByWidthNative(TextWrapper text, int wrapWidth) {
            Object textComponent = text.get();
            return ChatMessages.breakRenderedChatMessageLines((StringVisitable) textComponent, wrapWidth, getThisWrapped())
                    .stream().map(TextWrapper::new).toList();
        }
        @Nullable
        public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
            Object lineComponent = textWrapper.get();
            Style style = null;
            if(lineComponent instanceof StringVisitable visitable) {
                style = getThisWrapped().getTextHandler().getStyleAt(visitable, mouseXFromLeft);
            }
            else if(lineComponent instanceof OrderedText text) {
                style = getThisWrapped().getTextHandler().getStyleAt(text, mouseXFromLeft);
            }
            return style != null ? new StyleWrapper(style) : null;
        }
    };}

}
