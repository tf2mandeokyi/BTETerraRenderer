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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_ATTRIBUTES_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_ATTRIBUTES_DECODER_H_

#include <vector>

#include "draco/compression/attributes/attributes_decoder_interface.h"
#include "draco/compression/point_cloud/point_cloud_decoder.h"
#include "draco/core/decoder_buffer.h"
#include "draco/draco_features.h"
#include "draco/point_cloud/point_cloud.h"

namespace draco {

// Base class for decoding one or more attributes that were encoded with a
// matching AttributesEncoder. It is a basic implementation of
// AttributesDecoderInterface that provides functionality that is shared between
// all AttributesDecoders.
class AttributesDecoder : public AttributesDecoderInterface {
 public:
  AttributesDecoder();
  virtual ~AttributesDecoder() = default;

  // Called after all attribute decoders are created. It can be used to perform
  // any custom initialization.
  bool Init(PointCloudDecoder *decoder, PointCloud *pc) override;

  // Decodes any attribute decoder specific data from the |in_buffer|.
  bool DecodeAttributesDecoderData(DecoderBuffer *in_buffer) override;

  int32_t GetAttributeId(int i) const override {
    return point_attribute_ids_[i];
  }
  int32_t GetNumAttributes() const override {
    return static_cast<int32_t>(point_attribute_ids_.size());
  }
  PointCloudDecoder *GetDecoder() const override {
    return point_cloud_decoder_;
  }

  // Decodes attribute data from the source buffer.
  bool DecodeAttributes(DecoderBuffer *in_buffer) override {
    if (!DecodePortableAttributes(in_buffer)) {
      return false;
    }
    if (!DecodeDataNeededByPortableTransforms(in_buffer)) {
      return false;
    }
    if (!TransformAttributesToOriginalFormat()) {
      return false;
    }
    return true;
  }

 protected:
  int32_t GetLocalIdForPointAttribute(int32_t point_attribute_id) const {
    const int id_map_size =
        static_cast<int>(point_attribute_to_local_id_map_.size());
    if (point_attribute_id >= id_map_size) {
      return -1;
    }
    return point_attribute_to_local_id_map_[point_attribute_id];
  }
  virtual bool DecodePortableAttributes(DecoderBuffer *in_buffer) = 0;
  virtual bool DecodeDataNeededByPortableTransforms(DecoderBuffer *in_buffer) {
    return true;
  }
  virtual bool TransformAttributesToOriginalFormat() { return true; }

 private:
  // List of attribute ids that need to be decoded with this decoder.
  std::vector<int32_t> point_attribute_ids_;

  // Map between point attribute id and the local id (i.e., the inverse of the
  // |point_attribute_ids_|.
  std::vector<int32_t> point_attribute_to_local_id_map_;

  PointCloudDecoder *point_cloud_decoder_;
  PointCloud *point_cloud_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_ATTRIBUTES_DECODER_H_

#include "draco/core/varint_decoding.h"

namespace draco {

AttributesDecoder::AttributesDecoder()
    : point_cloud_decoder_(nullptr), point_cloud_(nullptr) {}

bool AttributesDecoder::Init(PointCloudDecoder *decoder, PointCloud *pc) {
  point_cloud_decoder_ = decoder;
  point_cloud_ = pc;
  return true;
}

bool AttributesDecoder::DecodeAttributesDecoderData(DecoderBuffer *in_buffer) {
  // Decode and create attributes.
  uint32_t num_attributes;
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  if (point_cloud_decoder_->bitstream_version() <
      DRACO_BITSTREAM_VERSION(2, 0)) {
    if (!in_buffer->Decode(&num_attributes)) {
      return false;
    }
  } else
#endif
  {
    if (!DecodeVarint(&num_attributes, in_buffer)) {
      return false;
    }
  }

  // Check that decoded number of attributes is valid.
  if (num_attributes == 0) {
    return false;
  }
  if (num_attributes > 5 * in_buffer->remaining_size()) {
    // The decoded number of attributes is unreasonably high, because at least
    // five bytes of attribute descriptor data per attribute are expected.
    return false;
  }

  // Decode attribute descriptor data.
  point_attribute_ids_.resize(num_attributes);
  PointCloud *pc = point_cloud_;
  for (uint32_t i = 0; i < num_attributes; ++i) {
    // Decode attribute descriptor data.
    uint8_t att_type, data_type, num_components, normalized;
    if (!in_buffer->Decode(&att_type)) {
      return false;
    }
    if (!in_buffer->Decode(&data_type)) {
      return false;
    }
    if (!in_buffer->Decode(&num_components)) {
      return false;
    }
    if (!in_buffer->Decode(&normalized)) {
      return false;
    }
    if (att_type >= GeometryAttribute::NAMED_ATTRIBUTES_COUNT) {
      return false;
    }
    if (data_type == DT_INVALID || data_type >= DT_TYPES_COUNT) {
      return false;
    }

    // Check decoded attribute descriptor data.
    if (num_components == 0) {
      return false;
    }

    // Add the attribute to the point cloud.
    const DataType draco_dt = static_cast<DataType>(data_type);
    GeometryAttribute ga;
    ga.Init(static_cast<GeometryAttribute::Type>(att_type), nullptr,
            num_components, draco_dt, normalized > 0,
            DataTypeLength(draco_dt) * num_components, 0);
    uint32_t unique_id;
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
    if (point_cloud_decoder_->bitstream_version() <
        DRACO_BITSTREAM_VERSION(1, 3)) {
      uint16_t custom_id;
      if (!in_buffer->Decode(&custom_id)) {
        return false;
      }
      // TODO(draco-eng): Add "custom_id" to attribute metadata.
      unique_id = static_cast<uint32_t>(custom_id);
      ga.set_unique_id(unique_id);
    } else
#endif
    {
      if (!DecodeVarint(&unique_id, in_buffer)) {
        return false;
      }
      ga.set_unique_id(unique_id);
    }
    const int att_id = pc->AddAttribute(
        std::unique_ptr<PointAttribute>(new PointAttribute(ga)));
    pc->attribute(att_id)->set_unique_id(unique_id);
    point_attribute_ids_[i] = att_id;

    // Update the inverse map.
    if (att_id >=
        static_cast<int32_t>(point_attribute_to_local_id_map_.size())) {
      point_attribute_to_local_id_map_.resize(att_id + 1, -1);
    }
    point_attribute_to_local_id_map_[att_id] = i;
  }
  return true;
}

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AttributesDecoder implements AttributesDecoderInterface {

    private final CppVector<Integer> pointAttributeIds = CppVector.create(DataType.int32());
    private final CppVector<Integer> pointAttributeToLocalIdMap = CppVector.create(DataType.int32());
    private PointCloudDecoder pointCloudDecoder = null;
    private PointCloud pointCloud = null;

    @Override
    public Status init(PointCloudDecoder decoder, PointCloud pc) {
        this.pointCloudDecoder = decoder;
        this.pointCloud = pc;
        return Status.ok();
    }

    @Override
    public Status decodeAttributesDecoderData(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        // Decode and create attributes
        AtomicReference<UInt> numAttributesRef = new AtomicReference<>();
        if(pointCloudDecoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            if(inBuffer.decode(DataType.uint32(), numAttributesRef::set).isError(chain)) return chain.get();
        } else {
            if(inBuffer.decodeVarint(DataType.uint32(), numAttributesRef).isError(chain)) return chain.get();
        }
        UInt numAttributes = numAttributesRef.get();

        // Check that decoded number of attributes is valid
        if(numAttributes.equals(0)) {
            return Status.dracoError("Number of attributes is zero");
        }
        if(numAttributes.gt(5 * inBuffer.getRemainingSize())) {
            return Status.dracoError("Decoded number of attributes is unreasonably high");
        }

        // Decode attribute descriptor data
        pointAttributeIds.resize(numAttributes.intValue());
        PointCloud pc = this.pointCloud;
        for(UInt i = UInt.ZERO; i.lt(numAttributes); i = i.add(1)) {
            // Decode attribute descriptor data
            AtomicReference<UByte> attTypeRef = new AtomicReference<>();
            if(inBuffer.decode(DataType.uint8(), attTypeRef::set).isError(chain)) return chain.get();
            GeometryAttribute.Type attType = GeometryAttribute.Type.valueOf(attTypeRef.get());

            AtomicReference<UByte> dataTypeRef = new AtomicReference<>();
            if(inBuffer.decode(DataType.uint8(), dataTypeRef::set).isError(chain)) return chain.get();
            DracoDataType dataType = DracoDataType.valueOf(dataTypeRef.get());

            AtomicReference<UByte> numComponentsRef = new AtomicReference<>();
            if(inBuffer.decode(DataType.uint8(), numComponentsRef::set).isError(chain)) return chain.get();
            UByte numComponents = numComponentsRef.get();

            AtomicReference<UByte> normalizedRef = new AtomicReference<>();
            if(inBuffer.decode(DataType.uint8(), normalizedRef::set).isError(chain)) return chain.get();
            UByte normalized = normalizedRef.get();

            if(attType == GeometryAttribute.Type.INVALID) {
                return Status.dracoError("Invalid attribute type: " + attTypeRef.get());
            }
            if(dataType == DracoDataType.DT_INVALID) {
                return Status.dracoError("Invalid data type: " + dataTypeRef.get());
            }

            // Check decoded attribute descriptor data
            if(numComponents.equals(0)) {
                return Status.dracoError("Number of components is zero");
            }

            // Add the attribute to the point cloud
            GeometryAttribute ga = new GeometryAttribute();
            ga.init(attType, null, numComponents, dataType, normalized.gt(UByte.ZERO),
                    dataType.getDataTypeLength() * numComponents.intValue(), 0);
            AtomicReference<UInt> uniqueIdRef = new AtomicReference<>();
            if(pointCloudDecoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(1, 3)) {
                AtomicReference<UShort> customIdRef = new AtomicReference<>();
                if(inBuffer.decode(DataType.uint16(), customIdRef::set).isError(chain)) return chain.get();
                uniqueIdRef.set(customIdRef.get().uIntValue());
            }
            else {
                if(inBuffer.decodeVarint(DataType.uint32(), uniqueIdRef).isError(chain)) return chain.get();
            }
            UInt uniqueId = uniqueIdRef.get();
            ga.setUniqueId(uniqueId);
            int attId = pc.addAttribute(new PointAttribute(ga));
            pc.getAttribute(attId).setUniqueId(uniqueId);
            pointAttributeIds.set(i.intValue(), attId);

            // Update the inverse map
            if(attId >= pointAttributeToLocalIdMap.size()) {
                pointAttributeToLocalIdMap.resize(attId + 1, -1);
            }
            pointAttributeToLocalIdMap.set(attId, i.intValue());
        }
        return Status.ok();
    }

    @Override
    public Status decodeAttributes(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        if(this.decodePortableAttributes(inBuffer).isError(chain)) return chain.get();
        if(this.decodeDataNeededByPortableTransforms(inBuffer).isError(chain)) return chain.get();
        if(this.transformAttributesToOriginalFormat().isError(chain)) return chain.get();
        return Status.ok();
    }

    @Override
    public int getAttributeId(int i) {
        return this.pointAttributeIds.get(i);
    }

    @Override
    public int getNumAttributes() {
        return this.pointAttributeIds.size();
    }

    @Override
    public PointCloudDecoder getDecoder() {
        return this.pointCloudDecoder;
    }

    protected int getLocalIdForPointAttribute(int pointAttributeId) {
        int idMapSize = this.pointAttributeToLocalIdMap.size();
        if(pointAttributeId >= idMapSize) {
            return -1;
        }
        return this.pointAttributeToLocalIdMap.get(pointAttributeId);
    }

    protected abstract Status decodePortableAttributes(DecoderBuffer inBuffer);
    protected Status decodeDataNeededByPortableTransforms(DecoderBuffer inBuffer) {
        return Status.ok();
    }
    protected Status transformAttributesToOriginalFormat() {
        return Status.ok();
    }
}
