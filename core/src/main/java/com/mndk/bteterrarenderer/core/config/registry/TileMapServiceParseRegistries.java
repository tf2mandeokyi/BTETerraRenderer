package com.mndk.bteterrarenderer.core.config.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mndk.bteterrarenderer.core.tile.TileMapService;
import com.mndk.bteterrarenderer.core.tile.flat.FlatTileMapService;
import com.mndk.bteterrarenderer.core.tile.ogc3dtiles.Ogc3dTileMapService;

public class TileMapServiceParseRegistries {

    public static final BiMap<String, Class<? extends TileMapService>> TYPE_MAP = HashBiMap.create();

    static {
        TYPE_MAP.put("flat", FlatTileMapService.class);
        TYPE_MAP.put("ogc3dtiles", Ogc3dTileMapService.class);
    }
}
