package com.mndk.bteterrarenderer.core.tile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@RequiredArgsConstructor
public class TMSIdPair<TileId> extends Pair<TileMapService<TileId>, TileId> {

    private final TileMapService<TileId> left;
    private final TileId right;

    public TileId setValue(TileId value) {
        throw new UnsupportedOperationException();
    }
}
