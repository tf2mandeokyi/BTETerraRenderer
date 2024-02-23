package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.mcconnector.client.gui.screen.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GuiChatRenderEvent {
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRenderChat(RenderGameOverlayEvent.Chat event) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if(!(currentScreen instanceof AbstractGuiScreenImpl screenImpl)) return;
        if(!(screenImpl.delegate instanceof MapRenderingOptionsSidebar sidebar)) return;
        if(sidebar.side.get() != SidebarSide.LEFT) return;

        event.setPosX(sidebar.sidebarWidth.get().intValue());
    }
}
