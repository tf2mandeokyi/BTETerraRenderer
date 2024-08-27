package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRenderer;
import com.mndk.bteterrarenderer.core.event.ClientConnectionEvents;
import lombok.experimental.UtilityClass;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@UtilityClass
@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID, value = Dist.CLIENT)
public class ClientOngoingConnectionEvents {

    @SubscribeEvent
    public void onClientConnection(ClientPlayerNetworkEvent.LoggedInEvent event) {
        ClientConnectionEvents.onJoin();
    }

    @SubscribeEvent
    public void onClientDisconnection(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        ClientConnectionEvents.onLeave();
    }

}
