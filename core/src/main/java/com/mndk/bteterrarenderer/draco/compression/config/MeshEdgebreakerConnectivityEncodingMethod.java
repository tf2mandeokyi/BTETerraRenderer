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
 * List of all variant of the edgebreaker method that is used for compression
 * of mesh connectivity.
 */
@Getter @RequiredArgsConstructor
public enum MeshEdgebreakerConnectivityEncodingMethod {
    STANDARD(0),
    PREDICTIVE(1),  // Deprecated.
    VALENCE(2);

    private final int value;

    @Nullable
    public static MeshEdgebreakerConnectivityEncodingMethod valueOf(UByte value) {
        return valueOf(value.intValue());
    }

    @Nullable
    public static MeshEdgebreakerConnectivityEncodingMethod valueOf(int value) {
        for(MeshEdgebreakerConnectivityEncodingMethod method : values()) {
            if(method.value == value) return method;
        }
        return null;
    }
}
