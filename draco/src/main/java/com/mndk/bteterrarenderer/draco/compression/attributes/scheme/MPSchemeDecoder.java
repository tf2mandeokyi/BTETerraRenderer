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

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import lombok.AccessLevel;
import lombok.Getter;

public abstract class MPSchemeDecoder<DataT, CorrT> extends PSchemeDecoder<DataT, CorrT> {

    @Getter(AccessLevel.PROTECTED)
    private final MPSchemeData<?> meshData;

    public MPSchemeDecoder(PointAttribute attribute,
                           PSchemeDecodingTransform<DataT, CorrT> transform,
                           MPSchemeData<?> meshData) {
        super(attribute, transform);
        this.meshData = meshData;
    }
}