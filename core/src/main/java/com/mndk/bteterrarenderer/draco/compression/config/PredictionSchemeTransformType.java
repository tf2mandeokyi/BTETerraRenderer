package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/** List of all prediction scheme transforms used by our framework. */
@Getter @RequiredArgsConstructor
public enum PredictionSchemeTransformType {
    PREDICTION_TRANSFORM_NONE(-1),
    /**
     * Basic delta transform where the prediction is computed as difference the
     * predicted and original value.
     */
    PREDICTION_TRANSFORM_DELTA(0),
    /**
     * An improved delta transform where all computed delta values are wrapped
     * around a fixed interval which lowers the entropy.
     */
    PREDICTION_TRANSFORM_WRAP(1),
    /** Specialized transform for normal coordinates using inverted tiles. */
    PREDICTION_TRANSFORM_NORMAL_OCTAHEDRON(2),
    /**
     * Specialized transform for normal coordinates using canonicalized inverted
     * tiles.
     */
    PREDICTION_TRANSFORM_NORMAL_OCTAHEDRON_CANONICALIZED(3);

    /** The number of valid (non-negative) prediction scheme transform types. */
    public static final int NUM_PREDICTION_SCHEME_TRANSFORM_TYPES = (int) Stream.of(values())
            .filter(e -> e != PREDICTION_TRANSFORM_NONE)
            .count();

    private final int value;

    @Nullable
    public static PredictionSchemeTransformType valueOf(int value) {
        for (PredictionSchemeTransformType type : values()) {
            if (type.value == value) return type;
        }
        return null;
    }
}
