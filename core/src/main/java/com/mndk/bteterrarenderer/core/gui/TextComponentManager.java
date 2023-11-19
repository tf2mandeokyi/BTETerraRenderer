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

    @Nullable
    @SuppressWarnings("DataFlowIssue")
    public Object getTooltipTextFromStyleComponent(@Nonnull Object styleComponent) {
        return MixinUtil.notOverwritten(styleComponent);
    }

}
