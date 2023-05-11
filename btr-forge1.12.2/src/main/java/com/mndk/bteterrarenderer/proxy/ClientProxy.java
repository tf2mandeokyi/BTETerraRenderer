package com.mndk.bteterrarenderer.proxy;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.commands.ToggleMapCommand;
import com.mndk.bteterrarenderer.loader.ProjectionYamlLoader;
import com.mndk.bteterrarenderer.loader.TMSYamlLoader;
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
		mapOptionsKey = registerKey("options_ui", "category", Keyboard.KEY_GRAVE);
		mapToggleKey = registerKey("toggle", "category", Keyboard.KEY_R);
	}

	private static KeyBinding registerKey(String name, String category, int keyCode) {
		KeyBinding key = new KeyBinding("key." + BTETerraRendererConstants.MODID + "." + name, keyCode, category);
		ClientRegistry.registerKeyBinding(key);
		return key;
	}

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		try {
			String modConfigDirectory = event.getModConfigurationDirectory().getAbsolutePath();
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
		initializeKeys();
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		
		ClientCommandHandler.instance.registerCommand(new ToggleMapCommand());
		// ClientCommandHandler.instance.registerCommand(new OpenConfigCommand());
	}
}
