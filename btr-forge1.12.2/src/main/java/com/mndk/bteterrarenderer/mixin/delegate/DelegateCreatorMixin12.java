package com.mndk.bteterrarenderer.mixin.delegate;

import com.mndk.bteterrarenderer.core.gui.components.IGuiChat;
import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import com.mndk.bteterrarenderer.core.util.mixin.MixinDelegateCreator;
import com.mndk.bteterrarenderer.mod.util.mixin.delegate.IGuiChatImpl12;
import com.mndk.bteterrarenderer.mod.util.mixin.delegate.IResourceLocationImpl12;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MixinDelegateCreator.class, remap = false)
public class DelegateCreatorMixin12 {

    @Overwrite
    public IGuiChat newGuiChat() {
        return new IGuiChatImpl12();
    }

    @Overwrite
    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl12(new ResourceLocation(modId, location));
    }
}
