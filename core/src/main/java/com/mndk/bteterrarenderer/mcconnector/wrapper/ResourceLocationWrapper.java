package com.mndk.bteterrarenderer.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;

import javax.annotation.Nonnull;

public class ResourceLocationWrapper extends MinecraftNativeObjectWrapper {
    public ResourceLocationWrapper(@Nonnull Object delegate) {
        super(delegate);
    }

    public static ResourceLocationWrapper of(String modId, String location) {
        return MixinUtil.notOverwritten(modId, location);
    }
}
