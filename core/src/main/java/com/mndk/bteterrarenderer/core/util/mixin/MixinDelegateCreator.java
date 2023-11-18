package com.mndk.bteterrarenderer.core.util.mixin;

import com.mndk.bteterrarenderer.core.gui.components.IGuiChat;
import com.mndk.bteterrarenderer.core.util.minecraft.IResourceLocation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MixinDelegateCreator {

    public IGuiChat newGuiChat() {
        return MixinUtil.notOverwritten();
    }

    public IResourceLocation newResourceLocation(String modId, String location) {
        return MixinUtil.notOverwritten(modId, location);
    }

}
