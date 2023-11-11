package com.mndk.bteterrarenderer.ogc3dtiles;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class TileData {
    private final TileDataFormat dataFormat;
}