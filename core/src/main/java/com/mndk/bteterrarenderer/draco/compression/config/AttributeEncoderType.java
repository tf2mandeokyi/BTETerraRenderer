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

/**
 * List of various attribute encoders supported by our framework. The entries
 * are used as unique identifiers of the encoders and their values should not
 * be changed!
 */
@Getter @RequiredArgsConstructor
public enum AttributeEncoderType {
    BASIC_ATTRIBUTE_ENCODER(0),
    MESH_TRAVERSAL_ATTRIBUTE_ENCODER(1),
    KD_TREE_ATTRIBUTE_ENCODER(2);

    private final int value;
}
