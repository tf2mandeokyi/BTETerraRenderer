package com.mndk.bteterrarenderer.proxy.client;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.commands.ToggleMapCommand;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
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
		try {
			File modConfigDirectory = event.getModConfigurationDirectory();
			ProjectionYamlLoader.INSTANCE.refresh(modConfigDirectory);
			TMSYamlLoader.INSTANCE.refresh(modConfigDirectory);
		} catch(Exception e) {
			BTETerraRendererConstants.LOGGER.error("Error caught while parsing map yaml files!");
			e.printStackTrace();
		}
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		KeyMappings.registerKeys();
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		
		ClientCommandHandler.instance.registerCommand(new ToggleMapCommand());
		// ClientCommandHandler.instance.registerCommand(new OpenConfigCommand());
	}
}
