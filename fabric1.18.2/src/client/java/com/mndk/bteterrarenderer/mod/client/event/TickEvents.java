package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.mod.client.KeyBindings;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

@UtilityClass
public class TickEvents {

    public void registerEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(TickEvents::onStartTick);
    }

    public void onStartTick(MinecraftClient ignored) {
        while(KeyBindings.MAP_TOGGLE_KEY.wasPressed()) {
            BTETerraRendererConfig.toggleRender();
        }
        if(KeyBindings.MAP_OPTIONS_KEY.wasPressed()) {
            MapRenderingOptionsSidebar.open();
        }
    }
}
