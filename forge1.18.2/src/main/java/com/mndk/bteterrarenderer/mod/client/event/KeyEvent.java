package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.mod.client.KeyMappings;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.mod.config.MC18ForgeTomlConfigBuilder;
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
        if(KeyMappings.MAP_OPTIONS_KEY.consumeClick()) {
            // I don't like this
            ((MC18ForgeTomlConfigBuilder) BTETerraRendererConfig.SAVE_LOADER_INSTANCE.getConfigBuilder()).saveRenderState();
            MapRenderingOptionsSidebar.open();
        }
        else if(KeyMappings.MAP_TOGGLE_KEY.consumeClick()) {
            BTETerraRendererConfig.toggleRender();
        }
    }
}
