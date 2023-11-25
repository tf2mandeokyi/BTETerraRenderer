package com.mndk.bteterrarenderer.mod;

import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.core.server.CubeProviderServer;
import net.buildtheearth.terraplusplus.generator.EarthGenerator;
import net.buildtheearth.terraplusplus.generator.EarthGeneratorSettings;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {
	public void preInit(FMLPreInitializationEvent event) {}
	public void init(FMLInitializationEvent event) {}
	public void postInit(FMLPostInitializationEvent event) {}
	@SuppressWarnings("unused")
	public void serverStarting(FMLServerStartingEvent event) {}

	// TODO: Move this to somewhere else
	public static EarthGeneratorSettings getWorldEarthGeneratorSettings(World world) {
		IChunkProvider chunkProvider = world.getChunkProvider();
		if(!(chunkProvider instanceof CubeProviderServer)) return null;

		ICubeGenerator cubeGenerator = ((CubeProviderServer) chunkProvider).getCubeGenerator();
		if (!(cubeGenerator instanceof EarthGenerator)) return null;

		return ((EarthGenerator) cubeGenerator).settings;
	}
}
