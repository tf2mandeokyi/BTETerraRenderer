package com.mndk.bteterrarenderer.config;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID)
public class ConfigEventHandler {

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(BTETerraRenderer.MODID)) {
            BTRConfig.save();
        }
    }

}
