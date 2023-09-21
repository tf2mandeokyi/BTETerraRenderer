package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.mod.client.KeyMappings18;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.mod.config.BTETerraRendererConfigImpl18;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeyEvent18 {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if(KeyMappings18.MAP_OPTIONS_KEY.consumeClick()) {
            BTETerraRendererConfig.INSTANCE.refreshTileMapService();
            BTETerraRendererConfigImpl18.saveRenderState();
            MapRenderingOptionsSidebar.open();
        }
        else if(KeyMappings18.MAP_TOGGLE_KEY.consumeClick()) {
            BTETerraRendererConfig.INSTANCE.toggleRender();
        }
    }
}
