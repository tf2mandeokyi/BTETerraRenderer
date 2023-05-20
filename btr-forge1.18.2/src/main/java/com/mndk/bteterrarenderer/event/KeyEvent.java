package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.gui.MapRenderingOptionsSidebar;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyEvent {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(ClientModEvents.mapOptionsKey.consumeClick()) {
            BTRConfigConnector.refreshTileMapService();
            MapRenderingOptionsSidebar.open();
        }
        else if(ClientModEvents.mapToggleKey.consumeClick()) {
            BTRConfigConnector.INSTANCE.toggleRender();
            BTRConfigConnector.save();
        }
    }
}
