package com.mndk.bteterrarenderer.mcconnector.client.gui.text;

import com.mndk.bteterrarenderer.mcconnector.client.text.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.client.text.TextWrapper;

import javax.annotation.Nonnull;

public interface TextManager {
    TextWrapper fromJson(@Nonnull String json);
    TextWrapper fromString(@Nonnull String text);
    StyleWrapper emptyStyle();
    StyleWrapper styleWithColor(StyleWrapper styleWrapper, TextFormatCopy textColor);
    boolean handleClick(@Nonnull StyleWrapper styleWrapper);
}
