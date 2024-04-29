package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void preRender(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        context.getMatrices().push();

        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if(!(currentScreen instanceof AbstractGuiScreenImpl screenImpl)) return;
        if(!(screenImpl.delegate instanceof MapRenderingOptionsSidebar sidebar)) return;
        if(sidebar.side.get() != SidebarSide.LEFT) return;

        int translateX = sidebar.sidebarWidth.get().intValue();
        context.getMatrices().translate(translateX, 0, 0);
    }

    @Inject(method = "render", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void postRender(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        context.getMatrices().pop();
    }

    @Inject(method = "isChatFocused", at = @At(value = "RETURN"), cancellable = true)
    public void isChatFocused(CallbackInfoReturnable<Boolean> cir) {
        if(client.currentScreen instanceof AbstractGuiScreenImpl screenImpl) {
            cir.setReturnValue(screenImpl.delegate.isChatFocused());
        }
    }

}
