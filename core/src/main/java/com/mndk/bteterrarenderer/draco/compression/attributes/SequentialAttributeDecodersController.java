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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_ATTRIBUTE_DECODERS_CONTROLLER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_ATTRIBUTE_DECODERS_CONTROLLER_H_

#include "draco/compression/attributes/attributes_decoder.h"
#include "draco/compression/attributes/points_sequencer.h"
#include "draco/compression/attributes/sequential_attribute_decoder.h"

namespace draco {

// A basic implementation of an attribute decoder that decodes data encoded by
// the SequentialAttributeEncodersController class. The
// SequentialAttributeDecodersController creates a single
// AttributeIndexedValuesDecoder for each of the decoded attribute, where the
// type of the values decoder is determined by the unique identifier that was
// encoded by the encoder.
class SequentialAttributeDecodersController : public AttributesDecoder {
 public:
  explicit SequentialAttributeDecodersController(
      std::unique_ptr<PointsSequencer> sequencer);

  bool DecodeAttributesDecoderData(DecoderBuffer *buffer) override;
  bool DecodeAttributes(DecoderBuffer *buffer) override;
  const PointAttribute *GetPortableAttribute(
      int32_t point_attribute_id) override {
    const int32_t loc_id = GetLocalIdForPointAttribute(point_attribute_id);
    if (loc_id < 0) {
      return nullptr;
    }
    return sequential_decoders_[loc_id]->GetPortableAttribute();
  }

 protected:
  bool DecodePortableAttributes(DecoderBuffer *in_buffer) override;
  bool DecodeDataNeededByPortableTransforms(DecoderBuffer *in_buffer) override;
  bool TransformAttributesToOriginalFormat() override;
  virtual std::unique_ptr<SequentialAttributeDecoder> CreateSequentialDecoder(
      uint8_t decoder_type);

 private:
  std::vector<std::unique_ptr<SequentialAttributeDecoder>> sequential_decoders_;
  std::vector<PointIndex> point_ids_;
  std::unique_ptr<PointsSequencer> sequencer_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_ATTRIBUTE_DECODERS_CONTROLLER_H_

#ifdef DRACO_NORMAL_ENCODING_SUPPORTED
#include "draco/compression/attributes/sequential_normal_attribute_decoder.h"
#endif
#include "draco/compression/attributes/sequential_quantization_attribute_decoder.h"
#include "draco/compression/config/compression_shared.h"

namespace draco {

SequentialAttributeDecodersController::SequentialAttributeDecodersController(
    std::unique_ptr<PointsSequencer> sequencer)
    : sequencer_(std::move(sequencer)) {}

bool SequentialAttributeDecodersController::DecodeAttributesDecoderData(
    DecoderBuffer *buffer) {
  if (!AttributesDecoder::DecodeAttributesDecoderData(buffer)) {
    return false;
  }
  // Decode unique ids of all sequential encoders and create them.
  const int32_t num_attributes = GetNumAttributes();
  sequential_decoders_.resize(num_attributes);
  for (int i = 0; i < num_attributes; ++i) {
    uint8_t decoder_type;
    if (!buffer->Decode(&decoder_type)) {
      return false;
    }
    // Create the decoder from the id.
    sequential_decoders_[i] = CreateSequentialDecoder(decoder_type);
    if (!sequential_decoders_[i]) {
      return false;
    }
    if (!sequential_decoders_[i]->Init(GetDecoder(), GetAttributeId(i))) {
      return false;
    }
  }
  return true;
}

bool SequentialAttributeDecodersController::DecodeAttributes(
    DecoderBuffer *buffer) {
  if (!sequencer_ || !sequencer_->GenerateSequence(&point_ids_)) {
    return false;
  }
  // Initialize point to attribute value mapping for all decoded attributes.
  const int32_t num_attributes = GetNumAttributes();
  for (int i = 0; i < num_attributes; ++i) {
    PointAttribute *const pa =
        GetDecoder()->point_cloud()->attribute(GetAttributeId(i));
    if (!sequencer_->UpdatePointToAttributeIndexMapping(pa)) {
      return false;
    }
  }
  return AttributesDecoder::DecodeAttributes(buffer);
}

bool SequentialAttributeDecodersController::DecodePortableAttributes(
    DecoderBuffer *in_buffer) {
  const int32_t num_attributes = GetNumAttributes();
  for (int i = 0; i < num_attributes; ++i) {
    if (!sequential_decoders_[i]->DecodePortableAttribute(point_ids_,
                                                          in_buffer)) {
      return false;
    }
  }
  return true;
}

bool SequentialAttributeDecodersController::
    DecodeDataNeededByPortableTransforms(DecoderBuffer *in_buffer) {
  const int32_t num_attributes = GetNumAttributes();
  for (int i = 0; i < num_attributes; ++i) {
    if (!sequential_decoders_[i]->DecodeDataNeededByPortableTransform(
            point_ids_, in_buffer)) {
      return false;
    }
  }
  return true;
}

bool SequentialAttributeDecodersController::
    TransformAttributesToOriginalFormat() {
  const int32_t num_attributes = GetNumAttributes();
  for (int i = 0; i < num_attributes; ++i) {
    // Check whether the attribute transform should be skipped.
    if (GetDecoder()->options()) {
      const PointAttribute *const attribute =
          sequential_decoders_[i]->attribute();
      const PointAttribute *const portable_attribute =
          sequential_decoders_[i]->GetPortableAttribute();
      if (portable_attribute &&
          GetDecoder()->options()->GetAttributeBool(
              attribute->attribute_type(), "skip_attribute_transform", false)) {
        // Attribute transform should not be performed. In this case, we replace
        // the output geometry attribute with the portable attribute.
        // TODO(ostava): We can potentially avoid this copy by introducing a new
        // mechanism that would allow to use the final attributes as portable
        // attributes for predictors that may need them.
        sequential_decoders_[i]->attribute()->CopyFrom(*portable_attribute);
        continue;
      }
    }
    if (!sequential_decoders_[i]->TransformAttributeToOriginalFormat(
            point_ids_)) {
      return false;
    }
  }
  return true;
}

std::unique_ptr<SequentialAttributeDecoder>
SequentialAttributeDecodersController::CreateSequentialDecoder(
    uint8_t decoder_type) {
  switch (decoder_type) {
    case SEQUENTIAL_ATTRIBUTE_ENCODER_GENERIC:
      return std::unique_ptr<SequentialAttributeDecoder>(
          new SequentialAttributeDecoder());
    case SEQUENTIAL_ATTRIBUTE_ENCODER_INTEGER:
      return std::unique_ptr<SequentialAttributeDecoder>(
          new SequentialIntegerAttributeDecoder());
    case SEQUENTIAL_ATTRIBUTE_ENCODER_QUANTIZATION:
      return std::unique_ptr<SequentialAttributeDecoder>(
          new SequentialQuantizationAttributeDecoder());
#ifdef DRACO_NORMAL_ENCODING_SUPPORTED
    case SEQUENTIAL_ATTRIBUTE_ENCODER_NORMALS:
      return std::unique_ptr<SequentialNormalAttributeDecoder>(
          new SequentialNormalAttributeDecoder());
#endif
    default:
      break;
  }
  // Unknown or unsupported decoder type.
  return nullptr;
}

}  // namespace draco


 */

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.config.SequentialAttributeEncoderType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

import java.util.concurrent.atomic.AtomicReference;

public class SequentialAttributeDecodersController extends AttributesDecoder {

    private final CppVector<SequentialAttributeDecoder> sequentialDecoders = CppVector.create();
    private final CppVector<PointIndex> pointIds = CppVector.create(PointIndex.arrayManager());
    private final PointsSequencer sequencer;

    public SequentialAttributeDecodersController(PointsSequencer sequencer) {
        this.sequencer = sequencer;
    }

    @Override
    public Status decodeAttributesDecoderData(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(super.decodeAttributesDecoderData(inBuffer).isError(chain)) return chain.get();

        // Decode unique ids of all sequential encoders and create them.
        final int numAttributes = getNumAttributes();
        sequentialDecoders.resize(numAttributes);
        for(int i = 0; i < numAttributes; ++i) {
            AtomicReference<UByte> decoderTypeRef = new AtomicReference<>();
            if(inBuffer.decode(DataType.uint8(), decoderTypeRef::set).isError(chain)) return chain.get();
            SequentialAttributeEncoderType decoderType = SequentialAttributeEncoderType.valueOf(decoderTypeRef.get());
            if(decoderType == null) {
                return Status.ioError("Failed to decode sequential attribute encoder type");
            }

            // Create the decoder from the id.
            SequentialAttributeDecoder decoder = this.createSequentialDecoder(decoderType);
            if(decoder == null) return Status.ioError("Failed to create sequential decoder");
            if(decoder.init(this.getDecoder(), getAttributeId(i)).isError(chain)) return chain.get();

            sequentialDecoders.set(i, decoder);
        }
        return Status.ok();
    }

    @Override
    public Status decodeAttributes(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        if(sequencer == null) return Status.ioError("Sequencer is null");
        if(sequencer.generateSequence(pointIds).isError(chain)) return chain.get();
        // Initialize point to attribute value mapping for all decoded attributes.
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            PointAttribute pa = this.getDecoder().getPointCloud().getAttribute(getAttributeId(i));
            if(sequencer.updatePointToAttributeIndexMapping(pa).isError(chain)) return chain.get();
        }
        return super.decodeAttributes(inBuffer);
    }

    @Override
    public PointAttribute getPortableAttribute(int pointAttributeId) {
        int locId = getLocalIdForPointAttribute(pointAttributeId);
        if(locId < 0) return null;
        return sequentialDecoders.get(locId).getPortableAttribute();
    }

    @Override
    protected Status decodePortableAttributes(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            SequentialAttributeDecoder decoder = sequentialDecoders.get(i);
            if(decoder.decodePortableAttribute(pointIds, inBuffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    protected Status decodeDataNeededByPortableTransforms(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            SequentialAttributeDecoder decoder = sequentialDecoders.get(i);
            if(decoder.decodeDataNeededByPortableTransform(pointIds, inBuffer).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    @Override
    protected Status transformAttributesToOriginalFormat() {
        StatusChain chain = new StatusChain();
        final int numAttributes = getNumAttributes();
        for(int i = 0; i < numAttributes; ++i) {
            SequentialAttributeDecoder decoder = sequentialDecoders.get(i);
            // Check whether the attribute transform should be skipped.
            if (this.getDecoder().getOptions() != null) {
                PointAttribute attribute = decoder.getAttribute();
                PointAttribute portableAttribute = decoder.getPortableAttribute();
                if (portableAttribute != null && this.getDecoder().getOptions().getAttributeBool(
                        attribute.getAttributeType(), "skip_attribute_transform", false)) {
                    // Attribute transform should not be performed. In this case, we replace
                    // the output geometry attribute with the portable attribute.
                    sequentialDecoders.get(i).getAttribute().copyFrom(portableAttribute);
                    continue;
                }
            }
            if (decoder.transformAttributeToOriginalFormat(pointIds).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    protected SequentialAttributeDecoder createSequentialDecoder(SequentialAttributeEncoderType decoderType) {
        switch (decoderType) {
            case SEQUENTIAL_ATTRIBUTE_ENCODER_GENERIC: return new SequentialAttributeDecoder();
            case SEQUENTIAL_ATTRIBUTE_ENCODER_INTEGER: return new SequentialIntegerAttributeDecoder();
            case SEQUENTIAL_ATTRIBUTE_ENCODER_QUANTIZATION: return new SequentialQuantizationAttributeDecoder();
            default: return null; // Unknown or unsupported decoder type.
        }
    }

}
