package com.mndk.bteterrarenderer.client.event;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.client.KeyMappings;
import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.File;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        try {
            File modConfigDirectory = new File(Minecraft.getInstance().gameDirectory, "config");
            ProjectionYamlLoader.INSTANCE.refresh(modConfigDirectory);
            TMSYamlLoader.INSTANCE.refresh(modConfigDirectory);
        } catch(Exception e) {
            BTETerraRendererConstants.LOGGER.error("Error caught while parsing map yaml files!");
            e.printStackTrace();
        }
        KeyMappings.registerKeys();
        BTRConfigConnector.load();
        BTETerraRendererConstants.LOGGER.info("Done setting the mod up");
    }
}
