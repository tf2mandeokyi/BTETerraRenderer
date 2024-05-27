package com.mndk.bteterrarenderer.draco_deprecated;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** EdgeBreaker encoding methods */
public enum EdgeBreakerEncoding {
    STANDARD, // STANDARD_EDGEBREAKER
    VALENCE; // VALENCE_EDGEBREAKER

    private static final Map<Byte, EdgeBreakerEncoding> MAP = new HashMap<Byte, EdgeBreakerEncoding>() {{
        put((byte) 0, STANDARD);
        put((byte) 2, VALENCE);
    }};

    public static EdgeBreakerEncoding valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
