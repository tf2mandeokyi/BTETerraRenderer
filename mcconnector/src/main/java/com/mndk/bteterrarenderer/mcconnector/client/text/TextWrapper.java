package com.mndk.bteterrarenderer.mcconnector.client.text;

import com.mndk.bteterrarenderer.mcconnector.client.graphics.DrawContextWrapper;

import javax.annotation.Nullable;
import java.util.List;

public interface TextWrapper {
    int getWidth(FontWrapper fontWrapper);
    @Nullable
    StyleWrapper getStyleComponentFromLine(FontWrapper fontWrapper, int mouseXFromLeft);
    int drawWithShadow(FontWrapper fontWrapper, DrawContextWrapper drawContextWrapper, float x, float y, int color);
    List<? extends TextWrapper> splitByWidth(FontWrapper fontWrapper, int wrapWidth);
}
