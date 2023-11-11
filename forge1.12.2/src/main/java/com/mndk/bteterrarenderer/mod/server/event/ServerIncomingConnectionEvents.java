package com.mndk.bteterrarenderer.mod.server.event;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.mod.BTETerraRendererMod;
import com.mndk.bteterrarenderer.mod.CommonProxy;
import com.mndk.bteterrarenderer.mod.network.ServerWelcomeMessageImpl;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = BTETerraRendererConstants.MODID, value = Side.SERVER)
public class ServerIncomingConnectionEvents {

    @SubscribeEvent
    public static void onPlayerLoginToServer(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        World world = event.player.world;
        EarthGeneratorSettings generatorSettings = CommonProxy.getWorldEarthGeneratorSettings(world);
        if(generatorSettings != null) {
            try {
                net.buildtheearth.terraplusplus.projection.GeographicProjection projection =
                        Objects.requireNonNull(CommonProxy.getWorldEarthGeneratorSettings(world)).projection();
                BTETerraRendererMod.NETWORK_WRAPPER.sendTo(
                        new ServerWelcomeMessageImpl(projection), (EntityPlayerMP) player);
                return;
            } catch(IOException e) {
                BTETerraRendererConstants.LOGGER.error("Caught IOException while sending projection data", e);
            }
        }
        BTETerraRendererMod.NETWORK_WRAPPER.sendTo(new ServerWelcomeMessageImpl(), (EntityPlayerMP) player);
    }


}
