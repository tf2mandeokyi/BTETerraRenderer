package com.mndk.bteterrarenderer.mixin.delegate;

import com.mndk.bteterrarenderer.core.gui.components.IGuiChat;
import com.mndk.bteterrarenderer.core.util.mixin.MixinDelegateCreator;
import com.mndk.bteterrarenderer.core.util.mixin.delegate.IResourceLocation;
import com.mndk.bteterrarenderer.mod.mixin.delegate.IResourceLocationImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MixinDelegateCreator.class, remap = false)
public class DelegateCreatorMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public IGuiChat newGuiChat() {
        // TODO: Implement or delete this
        return null;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }
}