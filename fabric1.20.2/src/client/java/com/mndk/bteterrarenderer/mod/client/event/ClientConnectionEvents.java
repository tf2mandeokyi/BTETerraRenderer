package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.projection.Projections;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

@UtilityClass
public class ClientConnectionEvents {

    public void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register(ClientConnectionEvents::onJoin);
    }

    public void onJoin(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        Projections.setDefaultBTEProjection();
    }
}
