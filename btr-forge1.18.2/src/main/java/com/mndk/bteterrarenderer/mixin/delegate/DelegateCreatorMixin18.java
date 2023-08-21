package com.mndk.bteterrarenderer.mixin.delegate;

import com.mndk.bteterrarenderer.core.gui.components.IGuiChat;
import com.mndk.bteterrarenderer.core.util.mixin.MixinDelegateCreator;
import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import com.mndk.bteterrarenderer.mod.util.mixin.delegate.IResourceLocationImpl18;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MixinDelegateCreator.class, remap = false)
public class DelegateCreatorMixin18 {

    @Overwrite
    public IGuiChat newGuiChat() {
        // TODO: Implement or delete this
        return null;
    }

    @Overwrite
    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl18(new ResourceLocation(modId, location));
    }
}