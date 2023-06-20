package com.mndk.bteterrarenderer.proxy.client;

import com.mndk.bteterrarenderer.commands.ToggleMapCommand;
import com.mndk.bteterrarenderer.loader.ConfigLoaders;
import com.mndk.bteterrarenderer.proxy.CommonProxy;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		File gameConfigDirectory = event.getModConfigurationDirectory();
		ConfigLoaders.loadAll(gameConfigDirectory);
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		KeyMappings12.registerKeys();
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		
		ClientCommandHandler.instance.registerCommand(new ToggleMapCommand());
		// ClientCommandHandler.instance.registerCommand(new OpenConfigCommand());
	}
}
