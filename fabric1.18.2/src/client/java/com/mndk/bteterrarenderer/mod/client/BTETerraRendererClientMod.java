package com.mndk.bteterrarenderer.mod.client;

import com.mndk.bteterrarenderer.mod.client.command.CommandsRegisterer;
import com.mndk.bteterrarenderer.mod.client.event.RenderEvents;
import com.mndk.bteterrarenderer.mod.client.event.TickEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class BTETerraRendererClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CommandsRegisterer.register();
        KeyBindings.registerKeys();
        WorldRenderEvents.LAST.register(RenderEvents::onRender);
        ClientTickEvents.START_CLIENT_TICK.register(TickEvents::onStartTick);
    }
}
