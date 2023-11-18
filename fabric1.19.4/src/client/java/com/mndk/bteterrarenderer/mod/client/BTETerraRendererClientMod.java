package com.mndk.bteterrarenderer.mod.client;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.input.KeyBindings;
import com.mndk.bteterrarenderer.mod.client.command.CommandsRegisterer;
import com.mndk.bteterrarenderer.mod.client.event.ClientOngoingConnectionEvents;
import com.mndk.bteterrarenderer.mod.client.event.RenderEvents;
import com.mndk.bteterrarenderer.mod.client.event.TickEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class BTETerraRendererClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BTETerraRendererConfig.initialize(FabricLoader.getInstance().getConfigDir().toFile());
        CommandsRegisterer.register();
        KeyBindings.registerAll();

        // Events
        RenderEvents.registerEvents();
        TickEvents.registerEvents();
        ClientOngoingConnectionEvents.registerEvents();

        BTETerraRendererConstants.LOGGER.info("Client Mod BTETerraRenderer initialized");
    }
}
