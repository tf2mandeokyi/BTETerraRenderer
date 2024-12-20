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

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class PSchemeEncoder<DataT, CorrT>
        implements PSchemeTypedEncoderInterface<DataT, CorrT> {

    private final PointAttribute attribute;
    private final PSchemeEncodingTransform<DataT, CorrT> transform;

    @Override public DataNumberType<DataT> getDataType() { return transform.getDataType(); }
    @Override public DataNumberType<CorrT> getCorrType() { return transform.getCorrType(); }

    @Override
    public Status encodePredictionData(EncoderBuffer buffer) {
        return transform.encodeTransformData(buffer);
    }

    @Override public PointAttribute getAttribute() { return attribute; }
    @Override public int getNumParentAttributes() { return 0; }

    @Override
    public PointAttribute.Type getParentAttributeType(int i) {
        return GeometryAttribute.Type.INVALID;
    }

    @Override
    public Status setParentAttribute(PointAttribute att) {
        return Status.unsupportedFeature("No parent attribute needed");
    }

    @Override
    public boolean areCorrectionsPositive() {
        return transform.areCorrectionsPositive();
    }

    @Override
    public PredictionSchemeTransformType getTransformType() {
        return transform.getType();
    }
}
