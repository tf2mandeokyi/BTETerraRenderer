package com.mndk.bteterrarenderer.mod.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.core.projection.Projections;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Dist.CLIENT)
public class ServerClientConnectionEvent {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onClientConnection(ClientPlayerNetworkEvent.LoggedInEvent event) {
        Projections.setDefaultBTEProjection();
        ConfigLoaders.loadAll(true);
    }

}
