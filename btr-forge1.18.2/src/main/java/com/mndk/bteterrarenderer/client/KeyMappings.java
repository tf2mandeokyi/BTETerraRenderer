package com.mndk.bteterrarenderer.client;

import com.mndk.bteterrarenderer.BTETerraRendererConstants;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class KeyMappings {

    public static KeyMapping MAP_TOGGLE_KEY, MAP_OPTIONS_KEY;

    /**
     * Call this on {@link FMLClientSetupEvent}
     */
    public static void registerKeys() {
        MAP_TOGGLE_KEY = registerKey("toggle", "category", InputConstants.KEY_R);
        MAP_OPTIONS_KEY = registerKey("options_ui", "category", InputConstants.KEY_GRAVE);
    }

    private static KeyMapping registerKey(String name, String category, int keyCode) {
        KeyMapping key = new KeyMapping("key." + BTETerraRendererConstants.MODID + "." + name, keyCode, category);
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
