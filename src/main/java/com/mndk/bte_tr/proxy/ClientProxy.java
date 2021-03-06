package com.mndk.bte_tr.proxy;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.lwjgl.input.Keyboard;

import com.mndk.bte_tr.BTETerraRenderer;
import com.mndk.bte_tr.config.ConfigHandler;
import com.mndk.bte_tr.event.KeyEvent;
import com.mndk.bte_tr.map.TileMapJsonLoader;

import java.io.IOException;

public class ClientProxy extends CommonProxy {

    public static KeyBinding mapOptionsKey, mapToggleKey;

    public static void initializeKeys() {
        mapOptionsKey = new KeyBinding(
                I18n.format("key.bte_tr.maprenderer.options_ui"),
                Keyboard.KEY_V,
                I18n.format("key.bte_tr.maprenderer.category"));
        mapToggleKey = new KeyBinding(
                I18n.format("key.bte_tr.maprenderer.toggle"),
                Keyboard.KEY_B,
                I18n.format("key.bte_tr.maprenderer.category"));
        ClientRegistry.registerKeyBinding(mapOptionsKey);
        ClientRegistry.registerKeyBinding(mapToggleKey);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        try {
        	TileMapJsonLoader.load(event.getModConfigurationDirectory().getAbsolutePath());
        } catch(Exception e) {
        	BTETerraRenderer.logger.error("Error caught while parsing map json files!");
        	e.printStackTrace();
        }
    }
    
    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        initializeKeys();
        try {
            ConfigHandler.init();
        } catch (IOException e) {
        	BTETerraRenderer.logger.error("Error caught while parsing config file!");
            e.printStackTrace();
        }
        MinecraftForge.EVENT_BUS.register(KeyEvent.class);
    }
}
