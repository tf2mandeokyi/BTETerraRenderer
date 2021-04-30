package com.mndk.bteterrarenderer.proxy;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.commands.ToggleMapCommand;
import com.mndk.bteterrarenderer.config.ConfigHandler;
import com.mndk.bteterrarenderer.event.KeyEvent;
import com.mndk.bteterrarenderer.storage.TileMapYamlLoader;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	public static KeyBinding mapOptionsKey, mapToggleKey;

	public static void initializeKeys() {
		mapOptionsKey = new KeyBinding(
				I18n.format("key.bteterrarenderer.maprenderer.options_ui"),
				Keyboard.KEY_GRAVE,
				I18n.format("key.bteterrarenderer.maprenderer.category"));
		mapToggleKey = new KeyBinding(
				I18n.format("key.bteterrarenderer.maprenderer.toggle"),
				Keyboard.KEY_R,
				I18n.format("key.bteterrarenderer.maprenderer.category"));
		ClientRegistry.registerKeyBinding(mapOptionsKey);
		ClientRegistry.registerKeyBinding(mapToggleKey);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		try {
			TileMapYamlLoader.refresh(event.getModConfigurationDirectory().getAbsolutePath());
		} catch(Exception e) {
			BTETerraRenderer.logger.error("Error caught while parsing map json files!");
			e.printStackTrace();
		}
		try {
			ConfigHandler.refresh();
		} catch (IOException e) {
			BTETerraRenderer.logger.error("Error caught while parsing config file!");
			e.printStackTrace();
		}
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		
		initializeKeys();
		
		MinecraftForge.EVENT_BUS.register(KeyEvent.class);
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		
		ClientCommandHandler.instance.registerCommand(new ToggleMapCommand());
		// ClientCommandHandler.instance.registerCommand(new OpenConfigCommand());
	}
}
