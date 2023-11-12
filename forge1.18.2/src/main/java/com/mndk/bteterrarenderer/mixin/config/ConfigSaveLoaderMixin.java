package com.mndk.bteterrarenderer.mixin.config;

import com.mndk.bteterrarenderer.core.config.ConfigSaveLoader;
import com.mndk.bteterrarenderer.mod.config.MC18ForgeTomlConfigBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ConfigSaveLoader.class, remap = false)
public class ConfigSaveLoaderMixin {
    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public static <C> ConfigSaveLoader makeSaveLoader(Class<C> configClass) {
        return new ConfigSaveLoader(configClass, new MC18ForgeTomlConfigBuilder());
    }
}
