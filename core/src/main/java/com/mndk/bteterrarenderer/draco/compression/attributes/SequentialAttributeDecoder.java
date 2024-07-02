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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_ATTRIBUTE_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_ATTRIBUTE_DECODER_H_

#include "draco/compression/attributes/prediction_schemes/prediction_scheme_interface.h"
#include "draco/compression/point_cloud/point_cloud_decoder.h"
#include "draco/draco_features.h"

namespace draco {

// A base class for decoding attribute values encoded by the
// SequentialAttributeEncoder.
class SequentialAttributeDecoder {
 public:
  SequentialAttributeDecoder();
  virtual ~SequentialAttributeDecoder() = default;

  virtual bool Init(PointCloudDecoder *decoder, int attribute_id);

  // Initialization for a specific attribute. This can be used mostly for
  // standalone decoding of an attribute without an PointCloudDecoder.
  virtual bool InitializeStandalone(PointAttribute *attribute);

  // Performs lossless decoding of the portable attribute data.
  virtual bool DecodePortableAttribute(const std::vector<PointIndex> &point_ids,
                                       DecoderBuffer *in_buffer);

  // Decodes any data needed to revert portable transform of the decoded
  // attribute.
  virtual bool DecodeDataNeededByPortableTransform(
      const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer);

  // Reverts transformation performed by encoder in
  // SequentialAttributeEncoder::TransformAttributeToPortableFormat() method.
  virtual bool TransformAttributeToOriginalFormat(
      const std::vector<PointIndex> &point_ids);

  const PointAttribute *GetPortableAttribute();

  const PointAttribute *attribute() const { return attribute_; }
  PointAttribute *attribute() { return attribute_; }
  int attribute_id() const { return attribute_id_; }
  PointCloudDecoder *decoder() const { return decoder_; }

 protected:
  // Should be used to initialize newly created prediction scheme.
  // Returns false when the initialization failed (in which case the scheme
  // cannot be used).
  virtual bool InitPredictionScheme(PredictionSchemeInterface *ps);

  // The actual implementation of the attribute decoding. Should be overridden
  // for specialized decoders.
  virtual bool DecodeValues(const std::vector<PointIndex> &point_ids,
                            DecoderBuffer *in_buffer);

  void SetPortableAttribute(std::unique_ptr<PointAttribute> att) {
    portable_attribute_ = std::move(att);
  }

  PointAttribute *portable_attribute() { return portable_attribute_.get(); }

 private:
  PointCloudDecoder *decoder_;
  PointAttribute *attribute_;
  int attribute_id_;

  // Storage for decoded portable attribute (after lossless decoding).
  std::unique_ptr<PointAttribute> portable_attribute_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_ATTRIBUTE_DECODER_H_

namespace draco {

SequentialAttributeDecoder::SequentialAttributeDecoder()
    : decoder_(nullptr), attribute_(nullptr), attribute_id_(-1) {}

bool SequentialAttributeDecoder::Init(PointCloudDecoder *decoder,
                                      int attribute_id) {
  decoder_ = decoder;
  attribute_ = decoder->point_cloud()->attribute(attribute_id);
  attribute_id_ = attribute_id;
  return true;
}

bool SequentialAttributeDecoder::InitializeStandalone(
    PointAttribute *attribute) {
  attribute_ = attribute;
  attribute_id_ = -1;
  return true;
}

bool SequentialAttributeDecoder::DecodePortableAttribute(
    const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer) {
  if (attribute_->num_components() <= 0 ||
      !attribute_->Reset(point_ids.size())) {
    return false;
  }
  if (!DecodeValues(point_ids, in_buffer)) {
    return false;
  }
  return true;
}

bool SequentialAttributeDecoder::DecodeDataNeededByPortableTransform(
    const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer) {
  // Default implementation does not apply any transform.
  return true;
}

bool SequentialAttributeDecoder::TransformAttributeToOriginalFormat(
    const std::vector<PointIndex> &point_ids) {
  // Default implementation does not apply any transform.
  return true;
}

const PointAttribute *SequentialAttributeDecoder::GetPortableAttribute() {
  // If needed, copy point to attribute value index mapping from the final
  // attribute to the portable attribute.
  if (!attribute_->is_mapping_identity() && portable_attribute_ &&
      portable_attribute_->is_mapping_identity()) {
    portable_attribute_->SetExplicitMapping(attribute_->indices_map_size());
    for (PointIndex i(0);
         i < static_cast<uint32_t>(attribute_->indices_map_size()); ++i) {
      portable_attribute_->SetPointMapEntry(i, attribute_->mapped_index(i));
    }
  }
  return portable_attribute_.get();
}

bool SequentialAttributeDecoder::InitPredictionScheme(
    PredictionSchemeInterface *ps) {
  for (int i = 0; i < ps->GetNumParentAttributes(); ++i) {
    const int att_id = decoder_->point_cloud()->GetNamedAttributeId(
        ps->GetParentAttributeType(i));
    if (att_id == -1) {
      return false;  // Requested attribute does not exist.
    }
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
    if (decoder_->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 0)) {
      if (!ps->SetParentAttribute(decoder_->point_cloud()->attribute(att_id))) {
        return false;
      }
    } else
#endif
    {
      const PointAttribute *const pa = decoder_->GetPortableAttribute(att_id);
      if (pa == nullptr || !ps->SetParentAttribute(pa)) {
        return false;
      }
    }
  }
  return true;
}

bool SequentialAttributeDecoder::DecodeValues(
    const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer) {
  const int32_t num_values = static_cast<uint32_t>(point_ids.size());
  const int entry_size = static_cast<int>(attribute_->byte_stride());
  std::unique_ptr<uint8_t[]> value_data_ptr(new uint8_t[entry_size]);
  uint8_t *const value_data = value_data_ptr.get();
  int out_byte_pos = 0;
  // Decode raw attribute values in their original format.
  for (int i = 0; i < num_values; ++i) {
    if (!in_buffer->Decode(value_data, entry_size)) {
      return false;
    }
    attribute_->buffer()->Write(out_byte_pos, value_data, entry_size);
    out_byte_pos += entry_size;
  }
  return true;
}

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PredictionSchemeInterface;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

@Getter
public class SequentialAttributeDecoder {

    private PointCloudDecoder decoder = null;
    private PointAttribute attribute = null;
    private int attributeId = -1;

    private PointAttribute portableAttribute = null;

    public Status init(PointCloudDecoder decoder, int attributeId) {
        this.decoder = decoder;
        this.attribute = decoder.getPointCloud().getAttribute(attributeId);
        this.attributeId = attributeId;
        return Status.ok();
    }

    public Status initializeStandalone(PointAttribute attribute) {
        this.attribute = attribute;
        this.attributeId = -1;
        return Status.ok();
    }

    public Status decodePortableAttribute(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        if(attribute.getNumComponents().le(0)) {
            return Status.dracoError("Attribute has no components");
        }
        if(attribute.reset(pointIds.size()).isError(chain)) return chain.get();
        if(this.decodeValues(pointIds, inBuffer).isError(chain)) return chain.get();
        return Status.ok();
    }

    public Status decodeDataNeededByPortableTransform(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        // Default implementation does not apply any transform.
        return Status.ok();
    }

    public Status transformAttributeToOriginalFormat(CppVector<PointIndex> pointIds) {
        // Default implementation does not apply any transform.
        return Status.ok();
    }

    public PointAttribute getPortableAttribute() {
        // If needed, copy point to attribute value index mapping from the final
        // attribute to the portable attribute.
        if(!attribute.isMappingIdentity() && portableAttribute != null && portableAttribute.isMappingIdentity()) {
            portableAttribute.setExplicitMapping(attribute.indicesMapSize());
            for(PointIndex i : PointIndex.range(0, attribute.indicesMapSize())) {
                portableAttribute.setPointMapEntry(i, attribute.getMappedIndex(i));
            }
        }
        return portableAttribute;
    }

    protected Status initPredictionScheme(PredictionSchemeInterface ps) {
        StatusChain chain = new StatusChain();

        for(int i = 0; i < ps.getNumParentAttributes(); i++) {
            int attId = decoder.getPointCloud().getNamedAttributeId(ps.getParentAttributeType(i));
            if(attId == -1) {
                return Status.dracoError("Requested attribute does not exist");
            }
            if(decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(ps.setParentAttribute(decoder.getPointCloud().getAttribute(attId)).isError(chain)) return chain.get();
            }
            else {
                PointAttribute pa = decoder.getPortableAttribute(attId);
                if (pa == null) {
                    return Status.dracoError("Requested attribute does not exist");
                }
                if (ps.setParentAttribute(pa).isError(chain)) return chain.get();
            }
        }
        return Status.ok();
    }

    protected Status decodeValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        int numValues = pointIds.size();
        int entrySize = (int) attribute.getByteStride();
        AtomicReference<UByteArray> valueDataRef = new AtomicReference<>();
        int outBytePos = 0;
        for(int i = 0; i < numValues; i++) {
            DataType<UByteArray, ?> entryType = DataType.bytes(entrySize);
            if(inBuffer.decode(entryType, valueDataRef::set).isError(chain)) return chain.get();
            UByteArray valueData = valueDataRef.get();
            attribute.getBuffer().write(entryType, outBytePos, valueData);
            outBytePos += entrySize;
        }
        return Status.ok();
    }

    protected void setPortableAttribute(PointAttribute att) {
        this.portableAttribute = att;
    }

}
