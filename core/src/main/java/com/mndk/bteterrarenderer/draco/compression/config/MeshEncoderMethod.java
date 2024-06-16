package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** List of encoding methods for meshes. */
@Getter @RequiredArgsConstructor
public enum MeshEncoderMethod {
    MESH_SEQUENTIAL_ENCODING(0),
    MESH_EDGEBREAKER_ENCODING(1);

    private final int value;
}
