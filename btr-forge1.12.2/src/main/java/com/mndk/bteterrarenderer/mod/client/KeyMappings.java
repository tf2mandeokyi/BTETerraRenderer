package com.mndk.bteterrarenderer.mod.client;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.lwjgl.input.Keyboard;

public class KeyMappings {

    public static KeyBinding MAP_OPTIONS_KEY, MAP_TOGGLE_KEY;

    /**
     * Call this on {@link FMLInitializationEvent} of {@code Side.CLIENT}
     */
    public static void registerKeys() {
        MAP_OPTIONS_KEY = registerKey("options_ui", Keyboard.KEY_GRAVE);
        MAP_TOGGLE_KEY = registerKey("toggle", Keyboard.KEY_R);
    }

    private static KeyBinding registerKey(String name, int keyCode) {
        KeyBinding key = new KeyBinding(
                "key." + BTETerraRendererConstants.MODID + "." + name, keyCode,
                "key." + BTETerraRendererConstants.MODID + ".category");
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
