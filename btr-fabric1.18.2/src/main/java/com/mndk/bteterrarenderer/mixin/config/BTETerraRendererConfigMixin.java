package com.mndk.bteterrarenderer.mixin.config;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.mod.config.BTETerraRendererConfigImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BTETerraRendererConfig.class, remap = false)
public class BTETerraRendererConfigMixin {
    @Inject(method = "saveConfiguration", at = @At("HEAD"))
    public void onSave(CallbackInfo ci) {
        BTETerraRendererConfigImpl.saveConfig();
    }

    @Inject(method = "loadConfiguration", at = @At("HEAD"))
    public void onLoad(CallbackInfo ci) {
        BTETerraRendererConfigImpl.readConfig();
    }
}
