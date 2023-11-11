package com.mndk.bteterrarenderer.mod.client;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.mod.client.command.CommandsRegisterer;
import com.mndk.bteterrarenderer.mod.client.event.ClientConnectionEvents;
import com.mndk.bteterrarenderer.mod.client.event.RenderEvents;
import com.mndk.bteterrarenderer.mod.client.event.TickEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BTETerraRendererClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConfigLoaders.setDirectoryAndLoadAll(FabricLoader.getInstance().getConfigDir().toFile());
        CommandsRegisterer.register();
        KeyBindings.registerKeys();

        // Events
        RenderEvents.registerEvents();
        TickEvents.registerEvents();
        ClientConnectionEvents.registerEvents();

        BTETerraRendererConstants.LOGGER.info("Client Mod BTETerraRenderer initialized");
    }
}
