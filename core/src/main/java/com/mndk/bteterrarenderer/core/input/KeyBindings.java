package com.mndk.bteterrarenderer.core.input;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;

public class KeyBindings {

    public static IKeyBinding MAP_TOGGLE_KEY;
    public static IKeyBinding MAP_OPTIONS_KEY;

    public static void registerAll() {
        MAP_TOGGLE_KEY = IKeyBinding.register("toggle", InputKey.KEY_R);
        MAP_OPTIONS_KEY = IKeyBinding.register("options_ui", InputKey.KEY_GRAVE_ACCENT);
    }

    public static void checkInputs() {
        while(KeyBindings.MAP_TOGGLE_KEY.wasPressed()) {
            BTETerraRendererConfig.toggleRender();
        }
        if(KeyBindings.MAP_OPTIONS_KEY.wasPressed()) {
            MapRenderingOptionsSidebar.open();
        }
    }
}
