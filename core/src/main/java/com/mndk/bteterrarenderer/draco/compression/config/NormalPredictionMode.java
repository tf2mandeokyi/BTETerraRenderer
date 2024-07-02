package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

@Getter @RequiredArgsConstructor
public enum NormalPredictionMode {
    ONE_TRIANGLE(0),  // To be deprecated.
    TRIANGLE_AREA(1);

    private final int value;

    @Nullable
    public static NormalPredictionMode valueOf(int value) {
        for(NormalPredictionMode mode : values()) {
            if(mode.value == value) return mode;
        }
        return null;
    }
}
