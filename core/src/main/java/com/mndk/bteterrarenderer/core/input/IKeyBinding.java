package com.mndk.bteterrarenderer.core.input;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.util.mixin.MixinUtil;

public interface IKeyBinding {

    static IKeyBinding register(String name, InputKey key) {
        return registerInternal(
                "key." + BTETerraRendererConstants.MODID + "." + name, key,
                "key." + BTETerraRendererConstants.MODID + ".category");
    }

    static IKeyBinding registerInternal(String description, InputKey key, String category) {
        return MixinUtil.notOverwritten(description, key, category);
    }

    boolean wasPressed();
}
