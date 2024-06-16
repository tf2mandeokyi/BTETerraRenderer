package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public enum NormalPredictionMode {
    ONE_TRIANGLE(0),  // To be deprecated.
    TRIANGLE_AREA(1);

    private final int value;
}
