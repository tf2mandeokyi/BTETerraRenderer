package com.mndk.bteterrarenderer.draco_deprecated.mesh;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public enum FaceEdge {
    LEFT, // LEFT_FACE_EDGE
    RIGHT; // RIGHT_FACE_EDGE

    private static final Map<Byte, FaceEdge> MAP = new HashMap<Byte, FaceEdge>() {{
        put((byte) 0, LEFT);
        put((byte) 1, RIGHT);
    }};

    public static FaceEdge valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
