/*
// Copyright 2016 The Draco Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
#ifndef DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_QUANTIZATION_ATTRIBUTE_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_QUANTIZATION_ATTRIBUTE_DECODER_H_

#include "draco/attributes/attribute_quantization_transform.h"
#include "draco/compression/attributes/sequential_integer_attribute_decoder.h"
#include "draco/draco_features.h"

namespace draco {

// Decoder for attribute values encoded with the
// SequentialQuantizationAttributeEncoder.
class SequentialQuantizationAttributeDecoder
    : public SequentialIntegerAttributeDecoder {
 public:
  SequentialQuantizationAttributeDecoder();
  bool Init(PointCloudDecoder *decoder, int attribute_id) override;

 protected:
  bool DecodeIntegerValues(const std::vector<PointIndex> &point_ids,
                           DecoderBuffer *in_buffer) override;
  bool DecodeDataNeededByPortableTransform(
      const std::vector<PointIndex> &point_ids,
      DecoderBuffer *in_buffer) override;
  bool StoreValues(uint32_t num_points) override;

  // Decodes data necessary for dequantizing the encoded values.
  virtual bool DecodeQuantizedDataInfo();

  // Dequantizes all values and stores them into the output attribute.
  virtual bool DequantizeValues(uint32_t num_values);

 private:
  AttributeQuantizationTransform quantization_transform_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_QUANTIZATION_ATTRIBUTE_DECODER_H_

 */

/*
// Copyright 2016 The Draco Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
#include "draco/compression/attributes/sequential_quantization_attribute_decoder.h"

#include "draco/core/quantization_utils.h"

namespace draco {

SequentialQuantizationAttributeDecoder::
    SequentialQuantizationAttributeDecoder() {}

bool SequentialQuantizationAttributeDecoder::Init(PointCloudDecoder *decoder,
                                                  int attribute_id) {
  if (!SequentialIntegerAttributeDecoder::Init(decoder, attribute_id)) {
    return false;
  }
  const PointAttribute *const attribute =
      decoder->point_cloud()->attribute(attribute_id);
  // Currently we can quantize only floating point arguments.
  if (attribute->data_type() != DT_FLOAT32) {
    return false;
  }
  return true;
}

bool SequentialQuantizationAttributeDecoder::DecodeIntegerValues(
    const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer) {
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  if (decoder()->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 0) &&
      !DecodeQuantizedDataInfo()) {
    return false;
  }
#endif
  return SequentialIntegerAttributeDecoder::DecodeIntegerValues(point_ids,
                                                                in_buffer);
}

bool SequentialQuantizationAttributeDecoder::
    DecodeDataNeededByPortableTransform(
        const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer) {
  if (decoder()->bitstream_version() >= DRACO_BITSTREAM_VERSION(2, 0)) {
    // Decode quantization data here only for files with bitstream version 2.0+
    if (!DecodeQuantizedDataInfo()) {
      return false;
    }
  }

  // Store the decoded transform data in portable attribute;
  return quantization_transform_.TransferToAttribute(portable_attribute());
}

bool SequentialQuantizationAttributeDecoder::StoreValues(uint32_t num_points) {
  return DequantizeValues(num_points);
}

bool SequentialQuantizationAttributeDecoder::DecodeQuantizedDataInfo() {
  // Get attribute used as source for decoding.
  auto att = GetPortableAttribute();
  if (att == nullptr) {
    // This should happen only in the backward compatibility mode. It will still
    // work fine for this case because the only thing the quantization transform
    // cares about is the number of components that is the same for both source
    // and target attributes.
    att = attribute();
  }
  return quantization_transform_.DecodeParameters(*att, decoder()->buffer());
}

bool SequentialQuantizationAttributeDecoder::DequantizeValues(
    uint32_t num_values) {
  // Convert all quantized values back to floats.
  return quantization_transform_.InverseTransformAttribute(
      *GetPortableAttribute(), attribute());
}

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.AttributeQuantizationTransform;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

public class SequentialQuantizationAttributeDecoder extends SequentialIntegerAttributeDecoder {

    private final AttributeQuantizationTransform quantizationTransform = new AttributeQuantizationTransform();

    @Override
    public Status init(PointCloudDecoder decoder, int attributeId) {
        StatusChain chain = new StatusChain();
        if (super.init(decoder, attributeId).isError(chain)) return chain.get();
        PointAttribute attribute = decoder.getPointCloud().getAttribute(attributeId);
        if (attribute.getDataType() != DracoDataType.DT_FLOAT32) {
            return Status.dracoError("Currently we can quantize only floating point arguments.");
        }
        return Status.ok();
    }

    @Override
    protected Status decodeIntegerValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(this.getDecoder().getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(this.decodeQuantizedDataInfo().isError(chain)) return chain.get();
        }
        return super.decodeIntegerValues(pointIds, inBuffer);
    }

    @Override
    public Status decodeDataNeededByPortableTransform(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(this.getDecoder().getBitstreamVersion() >= DracoVersions.getBitstreamVersion(2, 0)) {
            if(this.decodeQuantizedDataInfo().isError(chain)) return chain.get();
        }
        return this.quantizationTransform.transferToAttribute(this.getPortableAttribute());
    }

    @Override
    public Status storeValues(UInt numValues) {
        return this.dequantizeValues(numValues);
    }

    public Status decodeQuantizedDataInfo() {
        PointAttribute attribute = this.getPortableAttribute();
        if (attribute == null) attribute = this.getAttribute();
        return this.quantizationTransform.decodeParameters(attribute, this.getDecoder().getBuffer());
    }

    public Status dequantizeValues(UInt numValues) {
        return this.quantizationTransform.inverseTransformAttribute(
                this.getPortableAttribute(), this.getAttribute());
    }
}
