package com.mndk.bteterrarenderer.core.input;

import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.gui.MapRenderingOptionsSidebar;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.mcconnector.input.IKeyBinding;
import com.mndk.bteterrarenderer.mcconnector.input.InputKey;

public class KeyBindings {

    public static IKeyBinding MAP_TOGGLE_KEY;
    public static IKeyBinding MAP_OPTIONS_KEY;
    public static IKeyBinding MOVE_UP_KEY;
    public static IKeyBinding MOVE_DOWN_KEY;

    public static void registerAll() {
        MAP_TOGGLE_KEY = IKeyBinding.register(BTETerraRendererConstants.MODID, "toggle", InputKey.KEY_R);
        MAP_OPTIONS_KEY = IKeyBinding.register(BTETerraRendererConstants.MODID, "options_ui", InputKey.KEY_GRAVE_ACCENT);
        MOVE_UP_KEY = IKeyBinding.register(BTETerraRendererConstants.MODID, "move_up", InputKey.KEY_Y);
        MOVE_DOWN_KEY = IKeyBinding.register(BTETerraRendererConstants.MODID, "move_down", InputKey.KEY_I);
    }

    public static void checkInputs() {
        if(KeyBindings.MAP_TOGGLE_KEY.wasPressed()) {
            BTETerraRendererConfig.toggleRender();
        }
        if(KeyBindings.MAP_OPTIONS_KEY.wasPressed()) {
            MapRenderingOptionsSidebar.open();
        }
        while(KeyBindings.MOVE_UP_KEY.wasPressed()) {
            TileMapService<?> tms = BTETerraRendererConfig.getTileMapServiceWrapper().getItem();
            if(tms instanceof FlatTileMapService) {
                BTETerraRendererConfig.HOLOGRAM.flatMapYAxis += 0.5;
            } else {
                BTETerraRendererConfig.HOLOGRAM.yAlign += 0.5;
            }
        }
        while(KeyBindings.MOVE_DOWN_KEY.wasPressed()) {
            TileMapService<?> tms = BTETerraRendererConfig.getTileMapServiceWrapper().getItem();
            if(tms instanceof FlatTileMapService) {
                BTETerraRendererConfig.HOLOGRAM.flatMapYAxis -= 0.5;
            } else {
                BTETerraRendererConfig.HOLOGRAM.yAlign -= 0.5;
            }
        }
    }
}
