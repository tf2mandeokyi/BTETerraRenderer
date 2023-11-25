package com.mndk.bteterrarenderer.mixin.delegate;

import com.mndk.bteterrarenderer.core.util.minecraft.IResourceLocation;
import com.mndk.bteterrarenderer.core.util.mixin.MixinDelegateCreator;
import com.mndk.bteterrarenderer.mod.mixin.delegate.IResourceLocationImpl;
import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = MixinDelegateCreator.class, remap = false)
public class DelegateCreatorMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public IResourceLocation newResourceLocation(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }
}
