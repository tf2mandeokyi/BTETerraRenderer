package com.mndk.bteterrarenderer.mixin;

import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.mod.mcconnector.gui.AbstractGuiScreenImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "render", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void preRender(MatrixStack matrices, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        matrices.push();

        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if(!(currentScreen instanceof AbstractGuiScreenImpl screenImpl)) return;
        if(!(screenImpl.delegate instanceof MapRenderingOptionsSidebar sidebar)) return;
        if(sidebar.side.get() != SidebarSide.LEFT) return;

        int translateX = sidebar.sidebarWidth.get().intValue();
        matrices.translate(translateX, 0, 0);
    }

    @Inject(method = "render", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void postRender(MatrixStack matrices, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        matrices.pop();
    }

}
