package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WindowManager {
    public int getPixelWidth() { return MixinUtil.notOverwritten(); }
    public int getPixelHeight() { return MixinUtil.notOverwritten(); }
    public int getScaledWidth() { return MixinUtil.notOverwritten(); }
    public int getScaledHeight() { return MixinUtil.notOverwritten(); }
}
