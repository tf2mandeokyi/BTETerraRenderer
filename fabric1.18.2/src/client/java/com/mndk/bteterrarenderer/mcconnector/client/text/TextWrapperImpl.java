package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapperImpl;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TextWrapperImpl extends AbstractTextWrapper<Text> {

    protected TextWrapperImpl(@NotNull Text delegate) {
        super(delegate);
    }

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).getWrapped();
        return textRenderer.wrapLines(getWrapped(), wrapWidth).stream().map(OrderedTextWrapperImpl::new).toList();
    }

    public int getWidth(FontWrapper fontWrapper) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).getWrapped();
        return textRenderer.getWidth(getWrapped());
    }

    @Nullable
    public StyleWrapper getStyleComponentFromLine(FontWrapper fontWrapper, int mouseXFromLeft) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).getWrapped();
        Style style = textRenderer.getTextHandler().getStyleAt(getWrapped(), mouseXFromLeft);
        return style != null ? new StyleWrapperImpl(style) : null;
    }

    public int drawWithShadow(FontWrapper fontWrapper, DrawContextWrapper drawContextWrapper, float x, float y, int color) {
        MatrixStack matrixStack = ((DrawContextWrapperImpl) drawContextWrapper).getWrapped();
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).getWrapped();
        return textRenderer.drawWithShadow(matrixStack, getWrapped(), (int) x, (int) y, color);
    }
}
