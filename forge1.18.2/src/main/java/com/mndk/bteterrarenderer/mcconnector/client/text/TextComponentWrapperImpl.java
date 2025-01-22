package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.GuiDrawContextWrapperImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@RequiredArgsConstructor
public class TextComponentWrapperImpl extends AbstractTextWrapper {

    @Nonnull public final Component delegate;

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        Font font = ((FontWrapperImpl) fontWrapper).delegate;
        return font.split(delegate, wrapWidth).stream().map(FormattedCharSequenceWrapperImpl::new).toList();
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
}
