package com.mndk.bteterrarenderer.mixin.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.IResourceLocation;
import com.mndk.bteterrarenderer.mod.mcconnector.IResourceLocationImpl;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IResourceLocation.class, remap = false)
public interface IResourceLocationMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    static IResourceLocation of(String modId, String location) {
        return new IResourceLocationImpl(new ResourceLocation(modId, location));
    }

}