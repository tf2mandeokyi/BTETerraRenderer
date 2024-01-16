package com.mndk.bteterrarenderer.mixin.mcconnector.wrapper;

import com.mndk.bteterrarenderer.mcconnector.wrapper.ResourceLocationWrapper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ResourceLocationWrapper.class, remap = false)
public class ResourceLocationWrapperMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static ResourceLocationWrapper of(String modId, String location) {
        return new ResourceLocationWrapper(new Identifier(modId, location));
    }

}
