package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    public void onClose(CallbackInfo ci) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if(currentScreen instanceof AbstractGuiScreenImpl screenImpl) {
            boolean escapable = screenImpl.delegate.handleScreenEscape();
            if(!escapable) ci.cancel();
        }
    }

}
