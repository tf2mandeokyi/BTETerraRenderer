package com.mndk.bteterrarenderer.proxy;

import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.commands.ToggleMapCommand;
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
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

	// TODO delete "sidebarCheck" before release
	public static KeyBinding mapOptionsKey, mapToggleKey, sidebarCheck;

	public static void initializeKeys() {

		ClientRegistry.registerKeyBinding(mapOptionsKey = new KeyBinding(
				I18n.format("key.bteterrarenderer.maprenderer.options_ui"),
				Keyboard.KEY_GRAVE,
				I18n.format("key.bteterrarenderer.maprenderer.category"))
		);

		ClientRegistry.registerKeyBinding(mapToggleKey = new KeyBinding(
				I18n.format("key.bteterrarenderer.maprenderer.toggle"),
				Keyboard.KEY_R,
				I18n.format("key.bteterrarenderer.maprenderer.category"))
		);

		// TODO delete these before release
		ClientRegistry.registerKeyBinding(sidebarCheck = new KeyBinding(
				"Left sidebar check",
				Keyboard.KEY_HOME,
				I18n.format("key.bteterrarenderer.maprenderer.category"))
		);
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
