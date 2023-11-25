package com.mndk.bteterrarenderer.core.util.mixin;

import com.mndk.bteterrarenderer.core.util.minecraft.IResourceLocation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MixinDelegateCreator {
    public IResourceLocation newResourceLocation(String modId, String location) {
        return MixinUtil.notOverwritten(modId, location);
    }
}
