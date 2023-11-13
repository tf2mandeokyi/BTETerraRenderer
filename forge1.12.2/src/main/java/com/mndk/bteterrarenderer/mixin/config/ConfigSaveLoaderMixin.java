package com.mndk.bteterrarenderer.mixin.config;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.AbstractConfigSaveLoader;
import com.mndk.bteterrarenderer.mod.config.MC12ForgeCfgConfigSaveLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = AbstractConfigSaveLoader.class, remap = false)
public class ConfigSaveLoaderMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static <C> AbstractConfigSaveLoader makeSaveLoader(Class<C> configClass) {
        return new MC12ForgeCfgConfigSaveLoader(configClass, BTETerraRendererConstants.MODID);
    }
}
