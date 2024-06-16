package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/** List of all prediction methods currently supported by our framework. */
@Getter @RequiredArgsConstructor
public enum PredictionSchemeMethod {
    /** Special value indicating that no prediction scheme was used. */
    PREDICTION_NONE(-2),
    /** Used when no specific prediction scheme is required. */
    PREDICTION_UNDEFINED(-1),
    PREDICTION_DIFFERENCE(0),
    MESH_PREDICTION_PARALLELOGRAM(1),
    MESH_PREDICTION_MULTI_PARALLELOGRAM(2),
    MESH_PREDICTION_TEX_COORDS_DEPRECATED(3),
    MESH_PREDICTION_CONSTRAINED_MULTI_PARALLELOGRAM(4),
    MESH_PREDICTION_TEX_COORDS_PORTABLE(5),
    MESH_PREDICTION_GEOMETRIC_NORMAL(6);

    public static final int NUM_PREDICTION_SCHEMES = (int) Stream.of(values())
            .filter(e -> e != PREDICTION_NONE)
            .filter(e -> e != PREDICTION_UNDEFINED)
            .count();

    private final int value;
}
