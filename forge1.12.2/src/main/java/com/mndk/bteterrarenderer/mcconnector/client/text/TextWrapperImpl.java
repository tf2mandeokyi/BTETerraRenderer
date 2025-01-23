package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TextWrapperImpl extends AbstractTextWrapper {
    @Nonnull public final ITextComponent delegate;

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        FontRenderer font = ((FontWrapperImpl) fontWrapper).delegate;
        return GuiUtilRenderComponents.splitText(delegate, wrapWidth, font, true, false)
                .stream().map(TextWrapperImpl::new).collect(Collectors.toList());
    }

    public int getWidth(FontWrapper fontWrapper) {
        FontRenderer fontRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return fontRenderer.getStringWidth(delegate.getFormattedText());
    }

    @Nullable
    public StyleWrapper getStyleComponentFromLine(FontWrapper fontWrapper, int mouseXFromLeft) {
        int xPos = 0;
        ITextComponent clicked = null;
        for (ITextComponent child : delegate.getSiblings()) {
            int childWidth = fontWrapper.getWidth(new TextWrapperImpl(child));
            if (xPos <= mouseXFromLeft && mouseXFromLeft <= xPos + childWidth) {
                clicked = child; break;
            }
            xPos += childWidth;
        }
        return clicked != null ? new StyleWrapperImpl(clicked.getStyle()) : null;
    }

    public int drawWithShadow(FontWrapper fontWrapper, GuiDrawContextWrapper context, float x, float y, int color) {
        FontRenderer fontRenderer = ((FontWrapperImpl) fontWrapper).delegate;
        return fontRenderer.drawStringWithShadow(delegate.getFormattedText(), x, y, color);
    }
}
