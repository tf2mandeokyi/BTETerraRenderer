package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import lombok.Data;

@Data
public class TileLocalKey {
    private final int[] tileIndexes;
    private final int contentIndex;
}
