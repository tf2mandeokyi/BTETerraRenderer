package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.mod.client.mixin.graphics.AbstractGuiScreenImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/util/math/MatrixStack;I)V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    public void chatHudPreRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if(!(currentScreen instanceof AbstractGuiScreenImpl screenImpl)) return;
        if(!(screenImpl.delegate instanceof MapRenderingOptionsSidebar sidebar)) return;
        if(sidebar.side.get() != SidebarSide.LEFT) return;

        int translateX = sidebar.sidebarWidth.get().intValue();
        matrices.translate(translateX, 0, 0);
    }

}
