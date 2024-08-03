package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/** List of all prediction methods currently supported by our framework. */
@Getter @RequiredArgsConstructor
public enum PredictionSchemeMethod {
    /** Special value indicating that no prediction scheme was used. */
    NONE(-2),
    /** Used when no specific prediction scheme is required. */
    UNDEFINED(-1),
    DIFFERENCE(0),
    MESH_PARALLELOGRAM(1),
    MESH_MULTI_PARALLELOGRAM(2),
    MESH_TEX_COORDS_DEPRECATED(3),
    MESH_CONSTRAINED_MULTI_PARALLELOGRAM(4),
    MESH_TEX_COORDS_PORTABLE(5),
    MESH_GEOMETRIC_NORMAL(6);

    public static final int NUM_PREDICTION_SCHEMES = (int) Stream.of(values())
            .filter(e -> e != NONE)
            .filter(e -> e != UNDEFINED)
            .count();

    private final int value;

    @Nullable
    public static PredictionSchemeMethod valueOf(int value) {
        for (PredictionSchemeMethod type : values()) {
            if (type.value == value) return type;
        }
        return null;
    }
}
