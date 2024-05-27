package com.mndk.bteterrarenderer.draco_deprecated.mesh;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Mesh traversal methods */
public enum MeshTraversal {
    DEPTH_FIRST, // MESH_TRAVERSAL_DEPTH_FIRST
    PREDICTION_DEGREE; // MESH_TRAVERSAL_PREDICTION_DEGREE

    private static final Map<Byte, MeshTraversal> MAP = new HashMap<Byte, MeshTraversal>() {{
        put((byte) 0, DEPTH_FIRST);
        put((byte) 1, PREDICTION_DEGREE);
    }};

    public static MeshTraversal valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
