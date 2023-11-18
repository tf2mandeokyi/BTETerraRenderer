package com.mndk.bteterrarenderer.core.config.registry;

import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.Ogc3dTileMapService;

import java.util.HashMap;

public class TileMapServiceParseRegistries {

    public static final HashMap<String, Class<? extends TileMapService<?>>> TYPE_MAP = new HashMap<>();

    /* Mixin ready(?) */
    private static void registerAll() {
        TileMapServiceParseRegistries.TYPE_MAP.put("flat", FlatTileMapService.class);
        TileMapServiceParseRegistries.TYPE_MAP.put("ogc3dtiles", Ogc3dTileMapService.class);
    }

    static {
        registerAll();
    }

}
