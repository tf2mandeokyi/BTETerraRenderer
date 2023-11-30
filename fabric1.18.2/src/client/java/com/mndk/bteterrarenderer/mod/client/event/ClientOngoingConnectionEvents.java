package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.event.ClientConnectionEvents;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@UtilityClass
public class ClientOngoingConnectionEvents {
    public void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientConnectionEvents.onJoin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientConnectionEvents.onLeave());
    }
}
