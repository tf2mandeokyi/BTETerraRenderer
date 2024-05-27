package com.mndk.bteterrarenderer.draco_deprecated.sequential;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Sequential indices encoding methods */
public enum SequentialIndicesEncoding {
    COMPRESSED, // SEQUENTIAL_COMPRESSED_INDICES
    UNCOMPRESSED; // SEQUENTIAL_UNCOMPRESSED_INDICES

    private static final Map<Byte, SequentialIndicesEncoding> MAP = new HashMap<Byte, SequentialIndicesEncoding>() {{
        put((byte) 0, COMPRESSED);
        put((byte) 1, UNCOMPRESSED);
    }};

    public static SequentialIndicesEncoding valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
