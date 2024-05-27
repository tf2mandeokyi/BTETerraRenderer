package com.mndk.bteterrarenderer.draco_deprecated.prediction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Prediction scheme transform methods */
public enum PredictionSchemeTransform {
    WRAP, // PREDICTION_TRANSFORM_WRAP
    NORMAL_OCTAHEDRON_CANONICALIZED; // PREDICTION_TRANSFORM_NORMAL_OCTAHEDRON_CANONICALIZED

    private static final Map<Byte, PredictionSchemeTransform> MAP = new HashMap<Byte, PredictionSchemeTransform>() {{
        put((byte) 1, WRAP);
        put((byte) 3, NORMAL_OCTAHEDRON_CANONICALIZED);
    }};

    public static PredictionSchemeTransform valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
