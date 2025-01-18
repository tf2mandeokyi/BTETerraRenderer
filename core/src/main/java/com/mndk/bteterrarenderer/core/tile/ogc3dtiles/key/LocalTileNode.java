package com.mndk.bteterrarenderer.core.tile.ogc3dtiles.key;

import com.mndk.bteterrarenderer.ogc3dtiles.tile.TileContentLink;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Matrix4d;

@RequiredArgsConstructor
@Getter
public class LocalTileNode {
    private final TileLocalKey key;
    private final TileContentLink contentLink;
    private final Matrix4d transform;
}
