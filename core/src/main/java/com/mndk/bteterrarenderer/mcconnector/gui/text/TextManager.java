package com.mndk.bteterrarenderer.mcconnector.gui.text;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;

import javax.annotation.Nonnull;

public abstract class TextManager {

    public static final TextManager INSTANCE = makeDefault();
    private static TextManager makeDefault() {
        return MixinUtil.notOverwritten();
    }

    public abstract TextWrapper fromJson(String json);
    public abstract TextWrapper fromString(String text);
    public abstract StyleWrapper emptyStyle();
    public abstract StyleWrapper styleWithColor(StyleWrapper styleWrapper, TextFormatCopy textColor);
    public abstract boolean handleClick(@Nonnull StyleWrapper styleWrapper);
}
