package com.mndk.bteterrarenderer.draco_deprecated.sequential;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Sequential attribute encoding methods */
public enum SequentialAttributeEncoding {
    GENERIC, // SEQUENTIAL_ATTRIBUTE_ENCODER_GENERIC
    INTEGER, // SEQUENTIAL_ATTRIBUTE_ENCODER_INTEGER
    QUANTIZATION, // SEQUENTIAL_ATTRIBUTE_ENCODER_QUANTIZATION
    NORMALS; // SEQUENTIAL_ATTRIBUTE_ENCODER_NORMALS

    private static final Map<Byte, SequentialAttributeEncoding> MAP = new HashMap<Byte, SequentialAttributeEncoding>() {{
        put((byte) 0, GENERIC);
        put((byte) 1, INTEGER);
        put((byte) 2, QUANTIZATION);
        put((byte) 3, NORMALS);
    }};

    public static SequentialAttributeEncoding valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
