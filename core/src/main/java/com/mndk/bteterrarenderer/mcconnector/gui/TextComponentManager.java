package com.mndk.bteterrarenderer.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import com.mndk.bteterrarenderer.mcconnector.wrapper.DrawContextWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.StyleWrapper;
import com.mndk.bteterrarenderer.mcconnector.wrapper.TextWrapper;

import javax.annotation.Nonnull;

public abstract class TextComponentManager {

    public static final TextComponentManager INSTANCE = makeDefault();
    private static TextComponentManager makeDefault() {
        return MixinUtil.notOverwritten();
    }

    public abstract TextWrapper fromJson(String json);
    public abstract TextWrapper fromText(String text);
    public abstract boolean handleClick(@Nonnull StyleWrapper styleWrapper);
    public abstract void handleStyleComponentHover(DrawContextWrapper drawContextWrapper, StyleWrapper styleWrapper, int x, int y);
}
