package com.mndk.mapdisp4bte.proxy;

import com.mndk.mapdisp4bte.event.KeyEvent;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static KeyBinding mapOptionsKey, mapToggleKey;

    public static void initializeKeys() {
        mapOptionsKey = new KeyBinding(
                I18n.format("key.mapdisp4bte.maprenderer.options_ui"),
                Keyboard.KEY_V,
                I18n.format("key.mapdisp4bte.maprenderer.category"));
        mapToggleKey = new KeyBinding(
                I18n.format("key.mapdisp4bte.maprenderer.toggle"),
                Keyboard.KEY_B,
                I18n.format("key.mapdisp4bte.maprenderer.category"));
        ClientRegistry.registerKeyBinding(mapOptionsKey);
        ClientRegistry.registerKeyBinding(mapToggleKey);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        initializeKeys();
        MinecraftForge.EVENT_BUS.register(KeyEvent.class);
    }
}
