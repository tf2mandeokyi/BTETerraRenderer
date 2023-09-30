package com.mndk.bteterrarenderer.mod.client;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static KeyBinding MAP_TOGGLE_KEY, MAP_OPTIONS_KEY;

    public static void registerKeys() {
        MAP_TOGGLE_KEY = registerKey("toggle", GLFW.GLFW_KEY_R);
        MAP_OPTIONS_KEY = registerKey("options_ui", GLFW.GLFW_KEY_GRAVE_ACCENT);
    }

    private static KeyBinding registerKey(String name, int glfwKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + BTETerraRendererConstants.MODID + "." + name,
                InputUtil.Type.KEYSYM, glfwKey,
                "key." + BTETerraRendererConstants.MODID + ".category"
        ));
    }

}
