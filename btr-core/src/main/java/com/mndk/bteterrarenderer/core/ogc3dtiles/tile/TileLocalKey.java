package com.mndk.bteterrarenderer.core.ogc3dtiles.tile;

import lombok.Data;

@Data
public class TileLocalKey {
    private final int[] tileIndexes;
    private final int contentIndex;
}
