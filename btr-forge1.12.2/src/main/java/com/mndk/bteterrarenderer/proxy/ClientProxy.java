package com.mndk.bteterrarenderer.proxy;

import com.mndk.bteterrarenderer.BTETerraRendererCore;
import com.mndk.bteterrarenderer.commands.ToggleMapCommand;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	public static KeyBinding mapOptionsKey, mapToggleKey;

	public static void initializeKeys() {
		ClientRegistry.registerKeyBinding(mapOptionsKey = new KeyBinding(
				I18n.format("key.bteterrarenderer.options_ui"),
				Keyboard.KEY_GRAVE,
				I18n.format("key.bteterrarenderer.category"))
		);

		ClientRegistry.registerKeyBinding(mapToggleKey = new KeyBinding(
				I18n.format("key.bteterrarenderer.toggle"),
				Keyboard.KEY_R,
				I18n.format("key.bteterrarenderer.category"))
		);
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		try {
			String modConfigDirectory = event.getModConfigurationDirectory().getAbsolutePath();
			ProjectionYamlLoader.INSTANCE.refresh(modConfigDirectory);
			TMSYamlLoader.INSTANCE.refresh(modConfigDirectory);
		} catch(Exception e) {
			BTETerraRendererCore.logger.error("Error caught while parsing map yaml files!");
			e.printStackTrace();
		}
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		initializeKeys();
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		
		ClientCommandHandler.instance.registerCommand(new ToggleMapCommand());
		// ClientCommandHandler.instance.registerCommand(new OpenConfigCommand());
	}
}
