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

import com.mndk.bteterrarenderer.datatype.number.UByte;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum EncodedGeometryType {
    INVALID_GEOMETRY_TYPE(-1),
    POINT_CLOUD(0),
    TRIANGULAR_MESH(1);

    public static final int NUM_ENCODED_GEOMETRY_TYPES = (int) Stream.of(values())
            .filter(e -> e != INVALID_GEOMETRY_TYPE)
            .count();

    private final int value;

    EncodedGeometryType(int value) {
        this.value = value;
    }

    public static EncodedGeometryType valueOf(UByte value) {
        return valueOf(value.intValue());
    }
    public static EncodedGeometryType valueOf(int value) {
        for (EncodedGeometryType type : values()) {
            if (type.value == value) return type;
        }
        return INVALID_GEOMETRY_TYPE;
    }
}
