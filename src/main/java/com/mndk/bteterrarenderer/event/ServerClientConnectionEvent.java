package com.mndk.bteterrarenderer.event;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.network.ServerWelcomeMessage;
import com.mndk.bteterrarenderer.projection.Projections;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraplusplus.projection.GeographicProjection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = BTETerraRenderer.MODID)
public class ServerClientConnectionEvent {


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onClientConnects(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Projections.setDefaultBTEProjection();
    }



    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public static void onPlayerLoginToServer(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        World world = event.player.world;
        EarthGeneratorSettings generatorSettings = getEarthGeneratorSettings(world);

        if(generatorSettings != null) {
            BTETerraRenderer.NETWORK_WRAPPER.sendTo(
                    new ServerWelcomeMessage(generatorSettings), (EntityPlayerMP) player);
        }
    }



    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onPlayerLoginToLocalServer(PlayerEvent.PlayerLoggedInEvent event) {
        World world = event.player.world;
        EarthGeneratorSettings generatorSettings = getEarthGeneratorSettings(world);

        if(generatorSettings != null) {
            GeographicProjection projection = generatorSettings.projection();
            Projections.setServerProjection(projection);
        }
    }



    private static EarthGeneratorSettings getEarthGeneratorSettings(World world) {
        IChunkProvider chunkProvider = world.getChunkProvider();
        if(!(chunkProvider instanceof CubeProviderServer)) {
            return null;
        }

        ICubeGenerator cubeGenerator = ((CubeProviderServer) chunkProvider).getCubeGenerator();
        if (!(cubeGenerator instanceof EarthGenerator)) {
            return null;
        }

        return ((EarthGenerator) cubeGenerator).settings;
    }
}
