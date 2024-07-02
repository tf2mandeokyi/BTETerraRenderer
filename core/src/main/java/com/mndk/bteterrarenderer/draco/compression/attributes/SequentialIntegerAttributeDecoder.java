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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_INTEGER_ATTRIBUTE_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_INTEGER_ATTRIBUTE_DECODER_H_

#include "draco/compression/attributes/prediction_schemes/prediction_scheme_decoder.h"
#include "draco/compression/attributes/sequential_attribute_decoder.h"
#include "draco/draco_features.h"

namespace draco {

// Decoder for attributes encoded with the SequentialIntegerAttributeEncoder.
class SequentialIntegerAttributeDecoder : public SequentialAttributeDecoder {
 public:
  SequentialIntegerAttributeDecoder();
  bool Init(PointCloudDecoder *decoder, int attribute_id) override;

  bool TransformAttributeToOriginalFormat(
      const std::vector<PointIndex> &point_ids) override;

 protected:
  bool DecodeValues(const std::vector<PointIndex> &point_ids,
                    DecoderBuffer *in_buffer) override;
  virtual bool DecodeIntegerValues(const std::vector<PointIndex> &point_ids,
                                   DecoderBuffer *in_buffer);

  // Returns a prediction scheme that should be used for decoding of the
  // integer values.
  virtual std::unique_ptr<PredictionSchemeTypedDecoderInterface<int32_t>>
  CreateIntPredictionScheme(PredictionSchemeMethod method,
                            PredictionSchemeTransformType transform_type);

  // Returns the number of integer attribute components. In general, this
  // can be different from the number of components of the input attribute.
  virtual int32_t GetNumValueComponents() const {
    return attribute()->num_components();
  }

  // Called after all integer values are decoded. The implementation should
  // use this method to store the values into the attribute.
  virtual bool StoreValues(uint32_t num_values);

  void PreparePortableAttribute(int num_entries, int num_components);

  int32_t *GetPortableAttributeData() {
    if (portable_attribute()->size() == 0) {
      return nullptr;
    }
    return reinterpret_cast<int32_t *>(
        portable_attribute()->GetAddress(AttributeValueIndex(0)));
  }

 private:
  // Stores decoded values into the attribute with a data type AttributeTypeT.
  template <typename AttributeTypeT>
  void StoreTypedValues(uint32_t num_values);

  std::unique_ptr<PredictionSchemeTypedDecoderInterface<int32_t>>
      prediction_scheme_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_SEQUENTIAL_INTEGER_ATTRIBUTE_DECODER_H_

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
#include "draco/compression/attributes/sequential_integer_attribute_decoder.h"

#include "draco/compression/attributes/prediction_schemes/prediction_scheme_decoder_factory.h"
#include "draco/compression/attributes/prediction_schemes/prediction_scheme_wrap_decoding_transform.h"
#include "draco/compression/entropy/symbol_decoding.h"

namespace draco {

SequentialIntegerAttributeDecoder::SequentialIntegerAttributeDecoder() {}

bool SequentialIntegerAttributeDecoder::Init(PointCloudDecoder *decoder,
                                             int attribute_id) {
  if (!SequentialAttributeDecoder::Init(decoder, attribute_id)) {
    return false;
  }
  return true;
}

bool SequentialIntegerAttributeDecoder::TransformAttributeToOriginalFormat(
    const std::vector<PointIndex> &point_ids) {
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  if (decoder() &&
      decoder()->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 0)) {
    return true;  // Don't revert the transform here for older files.
  }
#endif
  return StoreValues(static_cast<uint32_t>(point_ids.size()));
}

bool SequentialIntegerAttributeDecoder::DecodeValues(
    const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer) {
  // Decode prediction scheme.
  int8_t prediction_scheme_method;
  if (!in_buffer->Decode(&prediction_scheme_method)) {
    return false;
  }
  // Check that decoded prediction scheme method type is valid.
  if (prediction_scheme_method < PREDICTION_NONE ||
      prediction_scheme_method >= NUM_PREDICTION_SCHEMES) {
    return false;
  }
  if (prediction_scheme_method != PREDICTION_NONE) {
    int8_t prediction_transform_type;
    if (!in_buffer->Decode(&prediction_transform_type)) {
      return false;
    }
    // Check that decoded prediction scheme transform type is valid.
    if (prediction_transform_type < PREDICTION_TRANSFORM_NONE ||
        prediction_transform_type >= NUM_PREDICTION_SCHEME_TRANSFORM_TYPES) {
      return false;
    }
    prediction_scheme_ = CreateIntPredictionScheme(
        static_cast<PredictionSchemeMethod>(prediction_scheme_method),
        static_cast<PredictionSchemeTransformType>(prediction_transform_type));
  }

  if (prediction_scheme_) {
    if (!InitPredictionScheme(prediction_scheme_.get())) {
      return false;
    }
  }

  if (!DecodeIntegerValues(point_ids, in_buffer)) {
    return false;
  }

#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  const int32_t num_values = static_cast<uint32_t>(point_ids.size());
  if (decoder() &&
      decoder()->bitstream_version() < DRACO_BITSTREAM_VERSION(2, 0)) {
    // For older files, revert the transform right after we decode the data.
    if (!StoreValues(num_values)) {
      return false;
    }
  }
#endif
  return true;
}

std::unique_ptr<PredictionSchemeTypedDecoderInterface<int32_t>>
SequentialIntegerAttributeDecoder::CreateIntPredictionScheme(
    PredictionSchemeMethod method,
    PredictionSchemeTransformType transform_type) {
  if (transform_type != PREDICTION_TRANSFORM_WRAP) {
    return nullptr;  // For now we support only wrap transform.
  }
  return CreatePredictionSchemeForDecoder<
      int32_t, PredictionSchemeWrapDecodingTransform<int32_t>>(
      method, attribute_id(), decoder());
}

bool SequentialIntegerAttributeDecoder::DecodeIntegerValues(
    const std::vector<PointIndex> &point_ids, DecoderBuffer *in_buffer) {
  const int num_components = GetNumValueComponents();
  if (num_components <= 0) {
    return false;
  }
  const size_t num_entries = point_ids.size();
  const size_t num_values = num_entries * num_components;
  PreparePortableAttribute(static_cast<int>(num_entries), num_components);
  int32_t *const portable_attribute_data = GetPortableAttributeData();
  if (portable_attribute_data == nullptr) {
    return false;
  }
  uint8_t compressed;
  if (!in_buffer->Decode(&compressed)) {
    return false;
  }
  if (compressed > 0) {
    // Decode compressed values.
    if (!DecodeSymbols(static_cast<uint32_t>(num_values), num_components,
                       in_buffer,
                       reinterpret_cast<uint32_t *>(portable_attribute_data))) {
      return false;
    }
  } else {
    // Decode the integer data directly.
    // Get the number of bytes for a given entry.
    uint8_t num_bytes;
    if (!in_buffer->Decode(&num_bytes)) {
      return false;
    }
    if (num_bytes == DataTypeLength(DT_INT32)) {
      if (portable_attribute()->buffer()->data_size() <
          sizeof(int32_t) * num_values) {
        return false;
      }
      if (!in_buffer->Decode(portable_attribute_data,
                             sizeof(int32_t) * num_values)) {
        return false;
      }
    } else {
      if (portable_attribute()->buffer()->data_size() <
          num_bytes * num_values) {
        return false;
      }
      if (in_buffer->remaining_size() <
          static_cast<int64_t>(num_bytes) * static_cast<int64_t>(num_values)) {
        return false;
      }
      for (size_t i = 0; i < num_values; ++i) {
        if (!in_buffer->Decode(portable_attribute_data + i, num_bytes)) {
          return false;
        }
      }
    }
  }

  if (num_values > 0 && (prediction_scheme_ == nullptr ||
                         !prediction_scheme_->AreCorrectionsPositive())) {
    // Convert the values back to the original signed format.
    ConvertSymbolsToSignedInts(
        reinterpret_cast<const uint32_t *>(portable_attribute_data),
        static_cast<int>(num_values), portable_attribute_data);
  }

  // If the data was encoded with a prediction scheme, we must revert it.
  if (prediction_scheme_) {
    if (!prediction_scheme_->DecodePredictionData(in_buffer)) {
      return false;
    }

    if (num_values > 0) {
      if (!prediction_scheme_->ComputeOriginalValues(
              portable_attribute_data, portable_attribute_data,
              static_cast<int>(num_values), num_components, point_ids.data())) {
        return false;
      }
    }
  }
  return true;
}

bool SequentialIntegerAttributeDecoder::StoreValues(uint32_t num_values) {
  switch (attribute()->data_type()) {
    case DT_UINT8:
      StoreTypedValues<uint8_t>(num_values);
      break;
    case DT_INT8:
      StoreTypedValues<int8_t>(num_values);
      break;
    case DT_UINT16:
      StoreTypedValues<uint16_t>(num_values);
      break;
    case DT_INT16:
      StoreTypedValues<int16_t>(num_values);
      break;
    case DT_UINT32:
      StoreTypedValues<uint32_t>(num_values);
      break;
    case DT_INT32:
      StoreTypedValues<int32_t>(num_values);
      break;
    default:
      return false;
  }
  return true;
}

template <typename AttributeTypeT>
void SequentialIntegerAttributeDecoder::StoreTypedValues(uint32_t num_values) {
  const int num_components = attribute()->num_components();
  const int entry_size = sizeof(AttributeTypeT) * num_components;
  const std::unique_ptr<AttributeTypeT[]> att_val(
      new AttributeTypeT[num_components]);
  const int32_t *const portable_attribute_data = GetPortableAttributeData();
  int val_id = 0;
  int out_byte_pos = 0;
  for (uint32_t i = 0; i < num_values; ++i) {
    for (int c = 0; c < num_components; ++c) {
      const AttributeTypeT value =
          static_cast<AttributeTypeT>(portable_attribute_data[val_id++]);
      att_val[c] = value;
    }
    // Store the integer value into the attribute buffer.
    attribute()->buffer()->Write(out_byte_pos, att_val.get(), entry_size);
    out_byte_pos += entry_size;
  }
}

void SequentialIntegerAttributeDecoder::PreparePortableAttribute(
    int num_entries, int num_components) {
  GeometryAttribute ga;
  ga.Init(attribute()->attribute_type(), nullptr, num_components, DT_INT32,
          false, num_components * DataTypeLength(DT_INT32), 0);
  std::unique_ptr<PointAttribute> port_att(new PointAttribute(ga));
  port_att->SetIdentityMapping();
  port_att->Reset(num_entries);
  port_att->set_unique_id(attribute()->unique_id());
  SetPortableAttribute(std::move(port_att));
}

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.AttributeValueIndex;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PredictionSchemeDecoderFactory;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PredictionSchemeTypedDecoderInterface;
import com.mndk.bteterrarenderer.draco.compression.attributes.scheme.PredictionSchemeWrapDecodingTransform;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeMethod;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolDecoding;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class SequentialIntegerAttributeDecoder extends SequentialAttributeDecoder {

    private PredictionSchemeTypedDecoderInterface<Integer, Integer> predictionScheme;

    @Override
    public Status init(PointCloudDecoder decoder, int attributeId) {
        return super.init(decoder, attributeId);
    }

    @Override
    public Status transformAttributeToOriginalFormat(CppVector<PointIndex> pointIds) {
        PointCloudDecoder decoder = this.getDecoder();
        if(decoder != null && this.getDecoder().getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            return Status.ok();  // Don't revert the transform here for older files.
        }
        return this.storeValues(UInt.of(pointIds.size()));
    }

    @Override
    protected Status decodeValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        // Decode prediction scheme.
        AtomicReference<Byte> predictionSchemeMethodRef = new AtomicReference<>();
        if(inBuffer.decode(DataType.int8(), predictionSchemeMethodRef::set).isError(chain)) return chain.get();
        PredictionSchemeMethod predictionSchemeMethod = PredictionSchemeMethod.valueOf(predictionSchemeMethodRef.get());

        // Check that decoded prediction scheme method type is valid.
        if (predictionSchemeMethod == null) return Status.ioError("Invalid prediction scheme method type");
        if(predictionSchemeMethod != PredictionSchemeMethod.PREDICTION_NONE) {
            AtomicReference<Byte> predictionTransformTypeRef = new AtomicReference<>();
            if(inBuffer.decode(DataType.int8(), predictionTransformTypeRef::set).isError(chain)) return chain.get();
            PredictionSchemeTransformType predictionTransformType =
                    PredictionSchemeTransformType.valueOf(predictionTransformTypeRef.get());

            // Check that decoded prediction scheme transform type is valid.
            if (predictionTransformType == null) {
                return Status.ioError("Invalid prediction scheme transform type");
            }
            this.predictionScheme = this.createIntPredictionScheme(predictionSchemeMethod, predictionTransformType);
        }

        if (this.predictionScheme != null) {
            if (this.initPredictionScheme(this.predictionScheme).isError(chain)) return chain.get();
        }

        if (this.decodeIntegerValues(pointIds, inBuffer).isError(chain)) return chain.get();

        int numValues = pointIds.size();
        PointCloudDecoder decoder = this.getDecoder();
        if (decoder != null && decoder.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
            // For older files, revert the transform right after we decode the data.
            if (this.storeValues(UInt.of(numValues)).isError(chain)) return chain.get();
        }
        return Status.ok();
    }

    protected Status decodeIntegerValues(CppVector<PointIndex> pointIds, DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();

        int numComponents = this.getNumValueComponents();
        if(numComponents <= 0) return Status.ioError("Invalid number of components: " + numComponents);

        int numEntries = pointIds.size();
        int numValues = numEntries * numComponents;
        this.preparePortableAttribute(numEntries, numComponents);
        CppVector<Integer> portableAttributeData = this.getPortableAttributeData();
        if(portableAttributeData == null) return Status.ioError("Portable attribute data is null");

        AtomicReference<UByte> compressedRef = new AtomicReference<>();
        if(inBuffer.decode(DataType.uint8(), compressedRef::set).isError(chain)) return chain.get();
        UByte compressed = compressedRef.get();

        if(compressed.gt(0)) {
            // Decode compressed values.
            CppVector<UInt> casted = portableAttributeData.cast(UInt::of, UInt::intValue);
            if(SymbolDecoding.decode(UInt.of(numValues), numComponents, inBuffer, casted).isError(chain)) return chain.get();
        }
        else {
            // Decode the integer data directly.
            // Get the number of bytes for a given entry.
            AtomicReference<UByte> numBytesRef = new AtomicReference<>();
            if(inBuffer.decode(DataType.uint8(), numBytesRef::set).isError(chain)) return chain.get();
            int numBytes = numBytesRef.get().intValue();

            long typeLength = DracoDataType.DT_INT32.getDataTypeLength();
            if(numBytes == typeLength) {
                if(this.getPortableAttribute().getBuffer().size() < typeLength * numValues) {
                    return Status.ioError("Portable attribute data is too small");
                }
                if(inBuffer.decode(DataType.int32(), portableAttributeData.setter(), numValues).isError(chain)) {
                    return chain.get();
                }
            }
            else {
                if(this.getPortableAttribute().getBuffer().size() < (long) numBytes * numValues) {
                    return Status.ioError("Portable attribute data is too small");
                }
                if(inBuffer.getRemainingSize() < (long) numBytes * numValues) {
                    return Status.ioError("Not enough data in the buffer");
                }
                for(int i = 0; i < numValues; i++) {
                    int finalI = i;
                    BiConsumer<Integer, Integer> setter = (j, val) -> portableAttributeData.set(finalI + j, val);
                    if(inBuffer.decode(DataType.int32(), setter, numBytes).isError(chain)) return chain.get();
                }
            }
        }

        if(numValues > 0 && (this.predictionScheme == null || !this.predictionScheme.areCorrectionsPositive())) {
            // Convert the values back to the original signed format.
            CppVector<UInt> casted = portableAttributeData.cast(UInt::of, UInt::intValue);
            BitUtils.convertSymbolsToSignedInts(casted, numValues, portableAttributeData);
        }

        // If the data was encoded with a prediction scheme, we must revert it.
        if(this.predictionScheme != null) {
            if(this.predictionScheme.decodePredictionData(inBuffer).isError(chain)) return chain.get();
            if(numValues > 0) {
                if(this.predictionScheme.computeOriginalValues(
                        portableAttributeData, portableAttributeData,
                        numValues, numComponents, pointIds).isError(chain)) return chain.get();
            }
        }
        return Status.ok();
    }

    protected PredictionSchemeTypedDecoderInterface<Integer, Integer> createIntPredictionScheme(
            PredictionSchemeMethod method, PredictionSchemeTransformType transformType) {
        if(transformType != PredictionSchemeTransformType.PREDICTION_TRANSFORM_WRAP) {
            return null;  // For now we support only wrap transform.
        }
        return PredictionSchemeDecoderFactory.createPredictionSchemeForDecoder(
                method, this.getAttribute().getUniqueId().intValue(), this.getDecoder(),
                new PredictionSchemeWrapDecodingTransform<>(DataType.int32(), DataType.int32()));
    }

    protected int getNumValueComponents() {
        return this.getAttribute().getNumComponents().intValue();
    }

    protected Status storeValues(UInt numValues) {
        DracoDataType dataType = this.getAttribute().getDataType();
        switch (dataType) {
            case DT_UINT8:  this.storeTypedValues(DataType.uint8(),  numValues); break;
            case DT_INT8:   this.storeTypedValues(DataType.int8(),   numValues); break;
            case DT_UINT16: this.storeTypedValues(DataType.uint16(), numValues); break;
            case DT_INT16:  this.storeTypedValues(DataType.int16(),  numValues); break;
            case DT_UINT32: this.storeTypedValues(DataType.uint32(), numValues); break;
            case DT_INT32:  this.storeTypedValues(DataType.int32(),  numValues); break;
            default: return Status.ioError("Invalid data type: " + dataType);
        }
        return Status.ok();
    }

    protected void preparePortableAttribute(int numEntries, int numComponents) {
        GeometryAttribute ga = new GeometryAttribute();
        DracoDataType dataType = DracoDataType.DT_INT32;
        ga.init(this.getAttribute().getAttributeType(), null, UByte.of(numComponents), dataType,
                false, numComponents * dataType.getDataTypeLength(), 0);
        PointAttribute portAtt = new PointAttribute(ga);
        portAtt.setIdentityMapping();
        portAtt.reset(numEntries);
        portAtt.setUniqueId(this.getAttribute().getUniqueId());
        this.setPortableAttribute(portAtt);
    }

    protected CppVector<Integer> getPortableAttributeData() {
        PointAttribute portableAttribute = this.getPortableAttribute();
        if(portableAttribute.size() == 0) return null;
        return portableAttribute.getValue(AttributeValueIndex.of(0), DataType.int32(), portableAttribute.size());
    }

    protected void setPortableAttributeData(CppVector<Integer> data) {
        PointAttribute portableAttribute = this.getPortableAttribute();
        AttributeValueIndex attIndex = AttributeValueIndex.of(0);
        portableAttribute.setAttributeValues(attIndex, DataType.int32(), data.getter(), data.size());
    }

    private <U, UArray> void storeTypedValues(DataNumberType<U, UArray> type, UInt numValues) {
        int numComponents = this.getAttribute().getNumComponents().intValue();
        int entrySize = (int) (type.size() * numComponents);
        UArray attVal = type.newArray(numComponents);
        CppVector<Integer> portableAttributeData = this.getPortableAttributeData();
        int valId = 0;
        int outBytePos = 0;
        for(int i = 0; i < numValues.intValue(); i++) {
            for(int c = 0; c < numComponents; c++) {
                U value = type.from(portableAttributeData.get(valId++));
                type.set(attVal, c, value);
            }
            this.getAttribute().getBuffer().write(type, outBytePos, attVal, entrySize);
            outBytePos += entrySize;
        }
    }
}
