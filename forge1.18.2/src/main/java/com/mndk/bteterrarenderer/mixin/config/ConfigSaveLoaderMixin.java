package com.mndk.bteterrarenderer.mixin.config;

import com.mndk.bteterrarenderer.core.config.saveloader.AbstractConfigSaveLoader;
import com.mndk.bteterrarenderer.mod.config.saveloader.MC18ForgeTomlConfigSaveLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = AbstractConfigSaveLoader.class, remap = false)
public class ConfigSaveLoaderMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static <C> AbstractConfigSaveLoader makeSaveLoader(Class<C> configClass) {
        return new MC18ForgeTomlConfigSaveLoader(configClass);
    }
}
