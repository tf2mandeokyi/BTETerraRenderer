package com.mndk.bteterrarenderer.draco_deprecated.mesh;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Mesh attribute encoding methods */
public enum MeshAttributeEncoding {
    VERTEX, // MESH_VERTEX_ATTRIBUTE
    CORNER; // MESH_CORNER_ATTRIBUTE

    private static final Map<Byte, MeshAttributeEncoding> MAP = new HashMap<Byte, MeshAttributeEncoding>() {{
        put((byte) 0, VERTEX);
        put((byte) 1, CORNER);
    }};

    public static MeshAttributeEncoding valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
