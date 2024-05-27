package com.mndk.bteterrarenderer.draco_deprecated.mesh;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Mesh encoding methods */
public enum MeshEncoding {
    SEQUENTIAL, // MESH_SEQUENTIAL_ENCODING
    EDGEBREAKER; // MESH_EDGEBREAKER_ENCODING

    private static final Map<Byte, MeshEncoding> MAP = new HashMap<Byte, MeshEncoding>() {{
        put((byte) 0, SEQUENTIAL);
        put((byte) 1, EDGEBREAKER);
    }};

    public static MeshEncoding valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
