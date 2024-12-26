package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapperImpl;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.TextCollector;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class OrderedTextWrapperImpl extends AbstractTextWrapper<OrderedText> {

    protected OrderedTextWrapperImpl(@NotNull OrderedText delegate) {
        super(delegate);
    }

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        TextRenderer textRenderer = ((FontWrapperImpl) fontWrapper).getWrapped();
        StringVisitable text = this.toStringVisitable();
        return textRenderer.wrapLines(text, wrapWidth).stream().map(OrderedTextWrapperImpl::new).toList();
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

    private StringVisitable toStringVisitable() {
        TextCollector textCollector = new TextCollector();
        AtomicReference<Style> lastStyle = new AtomicReference<>();
        AtomicReference<String> lastString = new AtomicReference<>("");
        getWrapped().accept((index, style, codePoint) -> {
            String append = new String(Character.toChars(codePoint));
            if (Objects.equals(style, lastStyle.get())) {
                lastString.set(lastString.get() + append);
                return true;
            }
            if (!lastString.get().isEmpty()) {
                textCollector.add(StringVisitable.styled(lastString.get(), lastStyle.get()));
            }
            lastStyle.set(style);
            lastString.set(append);
            return true;
        });
        if (!lastString.get().isEmpty()) {
            textCollector.add(StringVisitable.styled(lastString.get(), lastStyle.get()));
        }
        return textCollector.getCombined();
    }
}
