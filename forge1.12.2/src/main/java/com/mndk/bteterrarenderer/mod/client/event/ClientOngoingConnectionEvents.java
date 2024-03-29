package com.mndk.bteterrarenderer.mod.client.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.event.ClientConnectionEvents;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.mod.CommonProxy;
import lombok.experimental.UtilityClass;
import net.buildtheearth.terraplusplus.TerraConstants;
import net.buildtheearth.terraplusplus.dep.com.fasterxml.jackson.core.JsonProcessingException;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;

@UtilityClass
@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.CLIENT)
public class ClientOngoingConnectionEvents {

    @SubscribeEvent
    public void onClientConnection(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ClientConnectionEvents.onJoin();
    }

    @SubscribeEvent
    public void onClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        ClientConnectionEvents.onLeave();
    }

    @SubscribeEvent
    public void onPlayerLoginToLocalServer(PlayerEvent.PlayerLoggedInEvent event) throws JsonProcessingException {
        World world = event.player.world;
        EarthGeneratorSettings generatorSettings = CommonProxy.getWorldEarthGeneratorSettings(world);

        if(generatorSettings != null) {
            String projectionJson = TerraConstants.JSON_MAPPER.writeValueAsString(generatorSettings.projection());
            Projections.setServerProjection(GeographicProjection.parse(projectionJson));
        }
    }

}
