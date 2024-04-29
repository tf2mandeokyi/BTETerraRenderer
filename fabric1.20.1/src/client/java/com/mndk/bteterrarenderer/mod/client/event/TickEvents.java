package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.input.KeyBindings;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@UtilityClass
public class TickEvents {
    public void registerEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> KeyBindings.checkInputs());
    }
}
