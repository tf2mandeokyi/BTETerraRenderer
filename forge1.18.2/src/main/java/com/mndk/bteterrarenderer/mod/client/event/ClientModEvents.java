package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.input.KeyBindings;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
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
        File gameConfigDirectory = new File(Minecraft.getInstance().gameDirectory, "config");
        ConfigLoaders.setDirectoryAndLoadAll(gameConfigDirectory);
        KeyBindings.registerAll();

        BTETerraRendererConstants.LOGGER.info("Mod setup done");
    }
}
