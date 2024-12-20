package com.mndk.bteterrarenderer.mod.client;

import com.mndk.bteterrarenderer.core.BTETerraRendererCore;
import com.mndk.bteterrarenderer.core.input.KeyBindings;
import com.mndk.bteterrarenderer.util.Loggers;
import com.mndk.bteterrarenderer.mcconnector.client.ClientMinecraftManagerImpl;
import com.mndk.bteterrarenderer.mod.client.command.CommandsRegisterer;
import com.mndk.bteterrarenderer.mod.client.event.ClientOngoingConnectionEvents;
import com.mndk.bteterrarenderer.mod.client.event.RenderEvents;
import com.mndk.bteterrarenderer.mod.client.event.TickEvents;
import net.fabricmc.api.ClientModInitializer;

@SuppressWarnings("unused")
public class BTETerraRendererClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BTETerraRendererCore.initialize(new ClientMinecraftManagerImpl());
        CommandsRegisterer.register();
        KeyBindings.registerAll();

        // Events
        RenderEvents.registerEvents();
        TickEvents.registerEvents();
        ClientOngoingConnectionEvents.registerEvents();

        Loggers.get(this).info("Client Mod BTETerraRenderer initialized");
    }
}
