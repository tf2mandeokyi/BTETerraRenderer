package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.GuiSidebar;
import com.mndk.bteterrarenderer.core.gui.sidebar.SidebarSide;
import com.mndk.bteterrarenderer.mod.mixin.graphics.AbstractGuiScreenImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.CLIENT)
public class GuiChatRenderEvent {

    @SubscribeEvent
    public static void onRenderChat(RenderGameOverlayEvent.Chat event) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if(!(currentScreen instanceof AbstractGuiScreenImpl)) return;

        AbstractGuiScreenImpl screenImpl = (AbstractGuiScreenImpl) currentScreen;
        if(!(screenImpl.delegate instanceof MapRenderingOptionsSidebar)) return;

        GuiSidebar sidebar = (GuiSidebar) screenImpl.delegate;
        if(sidebar.side.get() != SidebarSide.LEFT) return;

        event.setPosX(sidebar.sidebarWidth.get().intValue());
    }

}
