package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class LocalTileNode {
    private final TileLocalKey key;
    private final TileContentLink contentLink;
    private final Matrix4f transform;
}
