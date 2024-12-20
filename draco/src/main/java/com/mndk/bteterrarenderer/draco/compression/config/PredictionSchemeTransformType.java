/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.compression.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/** List of all prediction scheme transforms used by our framework. */
@Getter @RequiredArgsConstructor
public enum PredictionSchemeTransformType {
    NONE(-1),
    /**
     * Basic delta transform where the prediction is computed as difference the
     * predicted and original value.
     */
    DELTA(0),
    /**
     * An improved delta transform where all computed delta values are wrapped
     * around a fixed interval which lowers the entropy.
     */
    WRAP(1),
    /** Specialized transform for normal coordinates using inverted tiles. */
    NORMAL_OCTAHEDRON(2),
    /**
     * Specialized transform for normal coordinates using canonicalized inverted
     * tiles.
     */
    NORMAL_OCTAHEDRON_CANONICALIZED(3);

    /** The number of valid (non-negative) prediction scheme transform types. */
    public static final int NUM_PREDICTION_SCHEME_TRANSFORM_TYPES = (int) Stream.of(values())
            .filter(e -> e != NONE)
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
