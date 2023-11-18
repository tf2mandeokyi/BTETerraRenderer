package com.mndk.bteterrarenderer.core.util.minecraft;

import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WindowManager {
    public int getPixelWidth() { return MixinUtil.notOverwritten(); }
    public int getPixelHeight() { return MixinUtil.notOverwritten(); }
    public int getScaledWidth() { return MixinUtil.notOverwritten(); }
    public int getScaledHeight() { return MixinUtil.notOverwritten(); }
}
