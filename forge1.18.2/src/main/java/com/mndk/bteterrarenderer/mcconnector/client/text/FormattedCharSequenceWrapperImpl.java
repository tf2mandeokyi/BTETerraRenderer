package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapperImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class FormattedCharSequenceWrapperImpl extends AbstractTextWrapper<FormattedCharSequence> {

    protected FormattedCharSequenceWrapperImpl(@NotNull FormattedCharSequence delegate) {
        super(delegate);
    }

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        Font font = ((FontWrapperImpl) fontWrapper).getWrapped();
        FormattedText text = this.toStringVisitable();
        return font.split(text, wrapWidth).stream().map(FormattedCharSequenceWrapperImpl::new).toList();
    }

    public int getWidth(FontWrapper fontWrapper) {
        Font font = ((FontWrapperImpl) fontWrapper).getWrapped();
        return font.width(getWrapped());
    }

    @Nullable
    public StyleWrapper getStyleComponentFromLine(FontWrapper fontWrapper, int mouseXFromLeft) {
        Font font = ((FontWrapperImpl) fontWrapper).getWrapped();
        Style style = font.getSplitter().componentStyleAtWidth(getWrapped(), mouseXFromLeft);
        return style != null ? new StyleWrapperImpl(style) : null;
    }

    public int drawWithShadow(FontWrapper fontWrapper, DrawContextWrapper drawContextWrapper, float x, float y, int color) {
        PoseStack poseStack = ((DrawContextWrapperImpl) drawContextWrapper).getWrapped();
        Font font = ((FontWrapperImpl) fontWrapper).getWrapped();
        return font.drawShadow(poseStack, getWrapped(), x, y, color);
    }

    private FormattedText toStringVisitable() {
        ComponentCollector textCollector = new ComponentCollector();
        AtomicReference<Style> lastStyle = new AtomicReference<>();
        AtomicReference<String> lastString = new AtomicReference<>("");
        getWrapped().accept((index, style, codePoint) -> {
            String append = new String(Character.toChars(codePoint));
            if (Objects.equals(style, lastStyle.get())) {
                lastString.set(lastString.get() + append);
                return true;
            }
            if (!lastString.get().isEmpty()) {
                textCollector.append(FormattedText.of(lastString.get(), lastStyle.get()));
            }
            lastStyle.set(style);
            lastString.set(append);
            return true;
        });
        if (!lastString.get().isEmpty()) {
            textCollector.append(FormattedText.of(lastString.get(), lastStyle.get()));
        }
        return textCollector.getResultOrEmpty();
    }
}
