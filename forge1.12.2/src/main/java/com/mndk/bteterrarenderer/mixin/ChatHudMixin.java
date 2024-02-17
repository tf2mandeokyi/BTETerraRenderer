package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiNewChat.class)
public class ChatHudMixin {

    @Shadow @Final private Minecraft mc;

    @Inject(method = "getChatOpen", at = @At(value = "RETURN"), cancellable = true)
    public void isChatFocused(CallbackInfoReturnable<Boolean> cir) {
        GuiScreen currentScreen = mc.currentScreen;
        if(currentScreen instanceof AbstractGuiScreenImpl) {
            AbstractGuiScreenImpl screenImpl = (AbstractGuiScreenImpl) currentScreen;
            cir.setReturnValue(screenImpl.delegate.isChatFocused());
        }
    }

}
