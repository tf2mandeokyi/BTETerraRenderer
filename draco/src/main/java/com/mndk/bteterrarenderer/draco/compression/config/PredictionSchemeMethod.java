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
