package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow @Nullable public GuiScreen currentScreen;

    @Inject(method = "displayGuiScreen", at = @At(value = "HEAD"), cancellable = true)
    public void preSetScreen(GuiScreen screen, CallbackInfo ci) {
        if(screen == null && currentScreen instanceof AbstractGuiScreenImpl) {
            AbstractGuiScreenImpl screenImpl = (AbstractGuiScreenImpl) currentScreen;
            boolean escapable = screenImpl.delegate.handleScreenEscape();
            if(!escapable) ci.cancel();
        }
    }

}
