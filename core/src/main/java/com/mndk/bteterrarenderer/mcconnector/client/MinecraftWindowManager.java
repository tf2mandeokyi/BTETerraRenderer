package com.mndk.bteterrarenderer.mcconnector.client;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;

public abstract class MinecraftWindowManager {

    public static final MinecraftWindowManager INSTANCE = makeInstance();
    private static MinecraftWindowManager makeInstance() {
        return MixinUtil.notOverwritten();
    }

    public abstract int getPixelWidth();
    public abstract int getPixelHeight();
    public abstract int getScaledWidth();
    public abstract int getScaledHeight();
}
