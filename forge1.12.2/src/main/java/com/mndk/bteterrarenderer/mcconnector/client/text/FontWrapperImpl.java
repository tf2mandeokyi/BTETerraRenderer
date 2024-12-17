package com.mndk.bteterrarenderer.mcconnector.client.text;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class FontWrapperImpl extends FontWrapper<FontRenderer> {

    public FontWrapperImpl(@Nonnull FontRenderer delegate) {
        super(delegate);
    }

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
    protected List<String> splitByWidthUnsafe(String string, int wrapWidth) {
        return getThisWrapped().listFormattedStringToWidth(string, wrapWidth);
    }
    protected List<TextWrapper> splitByWidthUnsafe(TextWrapper text, int wrapWidth) {
        ITextComponent textComponent = text.get();
        return GuiUtilRenderComponents.splitText(textComponent, wrapWidth, getThisWrapped(), true, false)
                .stream().map(TextWrapper::new).collect(Collectors.toList());
    }
    @Nullable
    public StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft) {
        int xPos = 0;
        ITextComponent clicked = null, textComponent = textWrapper.get();
        for (ITextComponent child : textComponent.getSiblings()) {
            int childWidth = this.getWidth(new TextWrapper(child));
            if (xPos <= mouseXFromLeft && mouseXFromLeft <= xPos + childWidth) {
                clicked = child; break;
            }
            xPos += childWidth;
        }
        return clicked != null ? new StyleWrapper(clicked.getStyle()) : null;
    }
}