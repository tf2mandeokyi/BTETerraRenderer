package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public enum EncodedGeometryType {
    INVALID_GEOMETRY_TYPE(-1),
    POINT_CLOUD(0),
    TRIANGULAR_MESH(1),
    NUM_ENCODED_GEOMETRY_TYPES(2);

    private final int value;
}
