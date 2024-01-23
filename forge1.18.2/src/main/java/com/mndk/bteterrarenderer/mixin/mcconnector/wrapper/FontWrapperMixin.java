package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.wrapper.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
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
@Mixin(value = FontWrapper.class, remap = false)
public class FontWrapperMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static FontWrapper<?> makeDefault() {
        return bTETerraRenderer$of(Minecraft.getInstance().font);
    }

    @Unique
    private static FontWrapper<?> bTETerraRenderer$of(Font font) { return new FontWrapper<Font>(font) {
        public int getHeight() {
            return getThisWrapped().lineHeight;
        }
        public int getWidth(String string) {
            return getThisWrapped().width(string);
        }
        public int getWidth(TextWrapper textWrapper) {
            Object textComponent = textWrapper.get();
            if(textComponent instanceof FormattedText text) {
                return getThisWrapped().width(text);
            }
            else if(textComponent instanceof FormattedCharSequence sequence) {
                return getThisWrapped().width(sequence);
            }
            return 0;
        }
        public String trimToWidth(String string, int width) {
            return getThisWrapped().plainSubstrByWidth(string, width);
        }
        public List<String> splitByWidth(String string, int wrapWidth) {
            return getThisWrapped().getSplitter().splitLines(string, wrapWidth, Style.EMPTY)
                    .stream().map(FormattedText::getString).toList();
        }
        public List<TextWrapper> splitByWidth(TextWrapper text, int wrapWidth) {
            Object textComponent = text.get();
            return ComponentRenderUtils.wrapComponents((FormattedText) textComponent, wrapWidth, getThisWrapped())
                    .stream().map(TextWrapper::new).toList();
        }
        @Nullable
        public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
            Object lineComponent = textWrapper.get();
            Style style = null;
            if(lineComponent instanceof FormattedText text) {
                style = getThisWrapped().getSplitter().componentStyleAtWidth(text, mouseXFromLeft);
            }
            else if(lineComponent instanceof FormattedCharSequence sequence) {
                style = getThisWrapped().getSplitter().componentStyleAtWidth(sequence, mouseXFromLeft);
            }
            return style != null ? new StyleWrapper(style) : null;
        }
    };}

}
