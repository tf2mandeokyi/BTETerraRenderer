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

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.AttributeOctahedronTransform;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeDecoderFactory;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeNormalOctahedronCanonicalizedDecodingTransform;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeNormalOctahedronDecodingTransform;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PSchemeTypedDecoderInterface;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class SequentialNormalAttributeDecoder extends SequentialIntegerAttributeDecoder {

    private final AttributeOctahedronTransform octahedralTransform = new AttributeOctahedronTransform();

    @Override
    public Status init(PointCloudDecoder decoder, int attributeId) {
        StatusChain chain = new StatusChain();
        if (super.init(decoder, attributeId).isError(chain)) return chain.get();
        if (!this.getAttribute().getNumComponents().equals(3)) {
            return Status.dracoError("This encoder works only for 3-component normal vectors.");
        }
        if (!this.getAttribute().getDataType().equals(DracoDataType.FLOAT32)) {
            return Status.dracoError("The data type must be DT_FLOAT32.");
        }
        return Status.ok();
    }

    @Override
    protected int getNumValueComponents() {
        return 2;
    }

    @Override
    protected Status decodeIntegerValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        if (this.getDecoder().getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if (octahedralTransform.decodeParameters(this.getAttribute(), inBuffer).isError(chain)) return chain.get();
        }
        return super.decodeIntegerValues(pointIds, inBuffer);
    }

    @Override
    public Status decodeDataNeededByPortableTransform(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        if (this.getDecoder().getBitstreamVersion() >= DracoVersions.getBitstreamVersion(2, 0)) {
            if (!octahedralTransform.decodeParameters(this.getPortableAttribute(), inBuffer).isError(chain)) return chain.get();
        }
        return octahedralTransform.transferToAttribute(this.getPortableAttributeInternal());
    }

    @Override
    protected Status storeValues(UInt numValues) {
        return octahedralTransform.inverseTransformAttribute(this.getPortableAttribute(), this.getAttribute());
    }

    public PSchemeTypedDecoderInterface<Integer, Integer> createIntPredictionScheme(
            PredictionSchemeMethod method, PredictionSchemeTransformType transformType) {
        switch (transformType) {
            case NORMAL_OCTAHEDRON:
                return PSchemeDecoderFactory.createPredictionSchemeForDecoder(
                        method, this.getAttributeId(), this.getDecoder(),
                        new PSchemeNormalOctahedronDecodingTransform<>(DataType.int32()));
            case NORMAL_OCTAHEDRON_CANONICALIZED:
                return PSchemeDecoderFactory.createPredictionSchemeForDecoder(
                        method, this.getAttributeId(), this.getDecoder(),
                        new PSchemeNormalOctahedronCanonicalizedDecodingTransform<>(DataType.int32()));
            default: return null;
        }
    }
}
