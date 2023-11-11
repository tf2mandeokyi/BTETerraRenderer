package com.mndk.bteterrarenderer.core.tile;

import lombok.Data;

@Data
public class TmsIdPair<TileId> {
    private final String tmsId;
    private final TileId tileId;
}
