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

/** List of encoding methods for point clouds. */
@Getter @RequiredArgsConstructor
public enum PointCloudEncodingMethod {
    POINT_CLOUD_SEQUENTIAL_ENCODING(0),
    POINT_CLOUD_KD_TREE_ENCODING(1);

    private final int value;
}
