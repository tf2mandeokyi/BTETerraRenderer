package com.mndk.bteterrarenderer.core.ogc3dtiles.tile;

import lombok.Data;

@Data
public class TileGlobalKey {
    private final TileLocalKey[] keys;
}
