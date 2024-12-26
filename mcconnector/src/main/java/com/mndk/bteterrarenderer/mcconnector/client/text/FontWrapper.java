package com.mndk.bteterrarenderer.mcconnector.client.text;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface FontWrapper {

    int getHeight();
    int getWidth(String string);
    int getWidth(TextWrapper text);
    String trimToWidth(String string, int width);
    @Nullable
    StyleWrapper getStyleComponentFromLine(@Nonnull TextWrapper textWrapper, int mouseXFromLeft);

    List<String> splitByWidth(String string, int wrapWidth);
    List<? extends TextWrapper> splitByWidth(TextWrapper text, int wrapWidth);
    int getWordWrappedHeight(String text, int maxWidth);
}
