package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapperImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class FormattedCharSequenceWrapperImpl extends AbstractTextWrapper {

    @Nonnull public final FormattedCharSequence delegate;

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        Font font = ((FontWrapperImpl) fontWrapper).delegate;
        FormattedText text = this.toStringVisitable();
        return font.split(text, wrapWidth).stream().map(FormattedCharSequenceWrapperImpl::new).toList();
    }

    public int getWidth(FontWrapper fontWrapper) {
        Font font = ((FontWrapperImpl) fontWrapper).delegate;
        return font.width(delegate);
    }

    @Nullable
    public StyleWrapper getStyleComponentFromLine(FontWrapper fontWrapper, int mouseXFromLeft) {
        Font font = ((FontWrapperImpl) fontWrapper).delegate;
        Style style = font.getSplitter().componentStyleAtWidth(delegate, mouseXFromLeft);
        return style != null ? new StyleWrapperImpl(style) : null;
    }

    public int drawWithShadow(FontWrapper fontWrapper, GuiDrawContextWrapper context, float x, float y, int color) {
        PoseStack poseStack = ((GuiDrawContextWrapperImpl) context).delegate;
        Font font = ((FontWrapperImpl) fontWrapper).delegate;
        return font.drawShadow(poseStack, delegate, x, y, color);
    }

    private FormattedText toStringVisitable() {
        ComponentCollector textCollector = new ComponentCollector();
        AtomicReference<Style> lastStyle = new AtomicReference<>();
        AtomicReference<String> lastString = new AtomicReference<>("");
        delegate.accept((index, style, codePoint) -> {
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
