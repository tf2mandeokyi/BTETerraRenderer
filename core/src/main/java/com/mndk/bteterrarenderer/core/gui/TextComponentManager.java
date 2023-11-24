package com.mndk.bteterrarenderer.core.gui;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@UtilityClass
public class TextComponentManager {

    public Object fromJson(String json) {
        return MixinUtil.notOverwritten(json);
    }

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public Object getStyleComponentFromLine(@Nonnull Object lineComponent, int mouseXFromLeft) {
        return MixinUtil.notOverwritten(lineComponent, mouseXFromLeft);
    }

    public boolean handleClick(@Nonnull Object styleComponent) {
        return MixinUtil.notOverwritten(styleComponent);
    }

    public void handleStyleComponentHover(@Nonnull Object poseStack, @Nonnull Object styleComponent, int x, int y) {
        MixinUtil.notOverwritten(poseStack, styleComponent, x, y);
    }

}
