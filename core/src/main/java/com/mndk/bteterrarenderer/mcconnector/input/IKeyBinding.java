package com.mndk.bteterrarenderer.mcconnector.input;

import com.mndk.bteterrarenderer.mcconnector.MixinUtil;

public interface IKeyBinding {

    static IKeyBinding register(String modId, String name, InputKey key) {
        return registerInternal("key." + modId + "." + name, key, "key." + modId + ".category");
    }

    static IKeyBinding registerInternal(String description, InputKey key, String category) {
        return MixinUtil.notOverwritten(description, key, category);
    }

    boolean wasPressed();
}
