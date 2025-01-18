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

import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;

public class MPSchemeDecoderFactory<DataT, CorrT>
        implements MPSchemeFactory<PSchemeDecoder<DataT, CorrT>,
        PSchemeDecodingTransform<DataT, CorrT>> {

    @Override
    public PSchemeDecoder<DataT, CorrT> create(
            PredictionSchemeMethod method, PointAttribute attribute,
            PSchemeDecodingTransform<DataT, CorrT> transform, MPSchemeData<?> meshData,
            UShort bitstreamVersion)
    {
        switch (method) {
            case MESH_PARALLELOGRAM:
                return new MPSchemeParallelogramDecoder<>(attribute, transform, meshData);
            case MESH_MULTI_PARALLELOGRAM:
                return new MPSchemeMultiParallelogramDecoder<>(attribute, transform, meshData);
            case MESH_CONSTRAINED_MULTI_PARALLELOGRAM:
                return new MPSchemeConstrainedMultiParallelogramDecoder<>(attribute, transform, meshData);
            case MESH_TEX_COORDS_DEPRECATED:
                return new MPSchemeTexCoordsDecoder<>(attribute, transform, meshData, bitstreamVersion);
            case MESH_TEX_COORDS_PORTABLE:
                return new MPSchemeTexCoordsPortableDecoder<>(attribute, transform, meshData);
            case MESH_GEOMETRIC_NORMAL:
                return new MPSchemeGeometricNormalDecoder<>(attribute, transform, meshData);
            default:
                return null;
        }
    }
}