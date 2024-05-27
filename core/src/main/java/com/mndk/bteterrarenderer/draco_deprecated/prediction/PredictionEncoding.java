package com.mndk.bteterrarenderer.draco_deprecated.prediction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Prediction encoding methods */
public enum PredictionEncoding {
    NONE, // PREDICTION_NONE
    DIFFERENCE, // PREDICTION_DIFFERENCE
    MESH_PARALLELOGRAM, // MESH_PREDICTION_PARALLELOGRAM
    MESH_CONSTRAINED_MULTI_PARALLELOGRAM, // MESH_PREDICTION_CONSTRAINED_MULTI_PARALLELOGRAM
    MESH_TEX_COORDS_PORTABLE, // MESH_PREDICTION_TEX_COORDS_PORTABLE
    MESH_GEOMETRIC_NORMAL; // MESH_PREDICTION_GEOMETRIC_NORMAL

    private static final Map<Byte, PredictionEncoding> MAP = new HashMap<Byte, PredictionEncoding>() {{
        put((byte) -2, NONE);
        put((byte) 0, DIFFERENCE);
        put((byte) 1, MESH_PARALLELOGRAM);
        put((byte) 4, MESH_CONSTRAINED_MULTI_PARALLELOGRAM);
        put((byte) 5, MESH_TEX_COORDS_PORTABLE);
        put((byte) 6, MESH_GEOMETRIC_NORMAL);
    }};

    public static PredictionEncoding valueOf(byte value) {
        return Objects.requireNonNull(MAP.get(value));
    }
}
