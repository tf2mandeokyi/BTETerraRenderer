package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapperImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TextComponentWrapperImpl extends AbstractTextWrapper<Component> {

    protected TextComponentWrapperImpl(@NotNull Component delegate) {
        super(delegate);
    }

    protected List<? extends TextWrapper> splitByWidthUnsafe(FontWrapper fontWrapper, int wrapWidth) {
        Font font = ((FontWrapperImpl) fontWrapper).getWrapped();
        return font.split(getWrapped(), wrapWidth).stream().map(FormattedCharSequenceWrapperImpl::new).toList();
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
}
