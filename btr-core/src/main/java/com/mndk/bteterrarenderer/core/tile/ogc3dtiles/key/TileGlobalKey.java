package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import lombok.Data;

@Data
public class TileGlobalKey {
    public static final TileGlobalKey ROOT_KEY = new TileGlobalKey(new TileLocalKey[0]);

    private final TileLocalKey[] keys;
}
