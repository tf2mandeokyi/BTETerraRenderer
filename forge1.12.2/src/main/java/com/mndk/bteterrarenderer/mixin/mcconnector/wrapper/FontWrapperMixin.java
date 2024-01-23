package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.wrapper.FontWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
@Mixin(value = FontWrapper.class, remap = false)
public class FontWrapperMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    private static FontWrapper<?> makeDefault() {
        return bTETerraRenderer$of(Minecraft.getMinecraft().fontRenderer);
    }

    @Unique
    private static FontWrapper<?> bTETerraRenderer$of(FontRenderer fontRenderer) { return new FontWrapper<FontRenderer>(fontRenderer) {
        public int getHeight() {
            return getThisWrapped().FONT_HEIGHT;
        }
        public int getWidth(String string) {
            return getThisWrapped().getStringWidth(string);
        }
        public int getWidth(TextWrapper text) {
            ITextComponent textComponent = text.get();
            return getThisWrapped().getStringWidth(textComponent.getFormattedText());
        }
        public String trimToWidth(String string, int width) {
            return getThisWrapped().trimStringToWidth(string, width);
        }
        public List<String> splitByWidth(String string, int wrapWidth) {
            return getThisWrapped().listFormattedStringToWidth(string, wrapWidth);
        }
        public List<TextWrapper> splitByWidth(TextWrapper text, int wrapWidth) {
            ITextComponent textComponent = text.get();
            return GuiUtilRenderComponents.splitText(textComponent, wrapWidth, getThisWrapped(), true, false)
                    .stream().map(TextWrapper::new).collect(Collectors.toList());
        }
        @Nullable
        public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
            int xPos = 0;
            ITextComponent clicked = null, textComponent = textWrapper.get();
            for(ITextComponent child : textComponent.getSiblings()) {
                int childWidth = this.getWidth(new TextWrapper(child));
                if(xPos <= mouseXFromLeft && mouseXFromLeft <= xPos + childWidth) {
                    clicked = child; break;
                }
                xPos += childWidth;
            }
            return clicked != null ? new StyleWrapper(clicked.getStyle()) : null;
        }
    };}

}