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
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.attributes.AttributeQuantizationTransform;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.SequentialAttributeEncoderType;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudEncoder;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class SequentialQuantizationAttributeEncoder extends SequentialIntegerAttributeEncoder {

    private final AttributeQuantizationTransform attributeQuantizationTransform = new AttributeQuantizationTransform();

    @Override
    public UByte getUniqueId() {
        return UByte.of(SequentialAttributeEncoderType.QUANTIZATION.getValue());
    }

    @Override
    public boolean isLossyEncoder() {
        return true;
    }

    @Override
    public Status init(PointCloudEncoder encoder, int attributeId) {
        StatusChain chain = new StatusChain();
        if (super.init(encoder, attributeId).isError(chain)) return chain.get();

        PointAttribute attribute = encoder.getPointCloud().getAttribute(attributeId);
        if (attribute.getDataType() != DracoDataType.FLOAT32) {
            return Status.dracoError("This encoder currently works only for floating point attributes.");
        }

        int quantizationBits = encoder.getOptions().getAttributeInt(attributeId, "quantization_bits", -1);
        if (quantizationBits < 1) {
            return Status.dracoError("Quantization bits must be greater than 0.");
        }
        if (encoder.getOptions().isAttributeOptionSet(attributeId, "quantization_origin") &&
           encoder.getOptions().isAttributeOptionSet(attributeId, "quantization_range")) {
            // Quantization settings are explicitly specified in the provided options.
            int numComponents = attribute.getNumComponents().intValue();
            CppVector<Float> quantizationOrigin = new CppVector<>(DataType.float32(), numComponents);
            encoder.getOptions().getAttributeVector(attributeId, "quantization_origin",
                    numComponents, quantizationOrigin.getPointer());

            float range = encoder.getOptions().getAttributeFloat(attributeId, "quantization_range", 1f);
            if (attributeQuantizationTransform.setParameters(quantizationBits, quantizationOrigin.getPointer(),
                    numComponents, range).isError(chain)) return chain.get();
        } else {
            // Compute quantization settings from the attribute values.
            if (attributeQuantizationTransform.computeParameters(attribute, quantizationBits).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    public Status encodeDataNeededByPortableTransform(EncoderBuffer outBuffer) {
        return attributeQuantizationTransform.encodeParameters(outBuffer);
    }

    @Override
    protected Status prepareValues(CppVector<PointIndex> pointIds, int numPoints) {
        StatusChain chain = new StatusChain();

        PointAttribute portableAttribute = attributeQuantizationTransform.initTransformedAttribute(
                this.getAttribute(), (int) pointIds.size());
        if (attributeQuantizationTransform.transformAttribute(
                this.getAttribute(), pointIds, portableAttribute).isError(chain)) return chain.get();

        this.setPortableAttribute(portableAttribute);
        return Status.ok();
    }
}
