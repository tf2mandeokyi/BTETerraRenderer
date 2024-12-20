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
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;

/**
 * List of various sequential attribute encoder/decoders that can be used in our
 * pipeline. The values represent unique identifiers used by the decoder and
 * they should not be changed.
 */
@Getter @RequiredArgsConstructor
public enum SequentialAttributeEncoderType {
    GENERIC(0),
    INTEGER(1),
    QUANTIZATION(2),
    NORMALS(3);

    private final int value;

    @Nullable
    public static SequentialAttributeEncoderType valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static SequentialAttributeEncoderType valueOf(int value) {
        for (SequentialAttributeEncoderType type : values()) {
            if (type.value == value) return type;
        }
        return null;
    }
}
