package com.mndk.bteterrarenderer.mcconnector.gui;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;

@UtilityClass
public class TextComponentManager {

    public Object fromJson(String json) {
        return MixinUtil.notOverwritten(json);
    }

    public Object fromText(String text) {
        return MixinUtil.notOverwritten(text);
    }

    public boolean handleClick(@Nonnull Object styleComponent) {
        return MixinUtil.notOverwritten(styleComponent);
    }

    public void handleStyleComponentHover(@Nonnull Object poseStack, @Nonnull Object styleComponent, int x, int y) {
        MixinUtil.notOverwritten(poseStack, styleComponent, x, y);
    }

}
