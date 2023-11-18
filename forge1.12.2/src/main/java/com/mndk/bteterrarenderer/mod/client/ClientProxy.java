package com.mndk.bteterrarenderer.mod.client;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.input.KeyBindings;
import com.mndk.bteterrarenderer.mod.CommonProxy;
import com.mndk.bteterrarenderer.mod.client.commands.ToggleMapCommand;
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
		BTETerraRendererConfig.initialize(gameConfigDirectory);
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		KeyBindings.registerAll();
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);

		ClientCommandHandler.instance.registerCommand(new ToggleMapCommand());
		// ClientCommandHandler.instance.registerCommand(new OpenConfigCommand());
	}
}
