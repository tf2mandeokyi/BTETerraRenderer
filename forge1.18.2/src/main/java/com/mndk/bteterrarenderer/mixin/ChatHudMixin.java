package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public class ChatHudMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "isChatFocused", at = @At(value = "RETURN"), cancellable = true)
    public void isChatFocused(CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.screen instanceof AbstractGuiScreenImpl screenImpl) {
            cir.setReturnValue(screenImpl.delegate.isChatFocused());
        }
    }

}
