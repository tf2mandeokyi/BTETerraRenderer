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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODER_H_
#define DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODER_H_

#include <type_traits>

#include "draco/compression/attributes/prediction_schemes/prediction_scheme_decoder_interface.h"
#include "draco/compression/attributes/prediction_schemes/prediction_scheme_decoding_transform.h"

// Prediction schemes can be used during encoding and decoding of vertex
// attributes to predict attribute values based on the previously
// encoded/decoded data. The differences between the original and predicted
// attribute values are used to compute correction values that can be usually
// encoded with fewer bits compared to the original data.
namespace draco {

// Abstract base class for typed prediction schemes. It provides basic access
// to the encoded attribute and to the supplied prediction transform.
template <typename DataTypeT,
          class TransformT =
              PredictionSchemeDecodingTransform<DataTypeT, DataTypeT>>
class PredictionSchemeDecoder : public PredictionSchemeTypedDecoderInterface<
                                    DataTypeT, typename TransformT::CorrType> {
 public:
  typedef DataTypeT DataType;
  typedef TransformT Transform;
  // Correction type needs to be defined in the prediction transform class.
  typedef typename Transform::CorrType CorrType;
  explicit PredictionSchemeDecoder(const PointAttribute *attribute)
      : PredictionSchemeDecoder(attribute, Transform()) {}
  PredictionSchemeDecoder(const PointAttribute *attribute,
                          const Transform &transform)
      : attribute_(attribute), transform_(transform) {}

  bool DecodePredictionData(DecoderBuffer *buffer) override {
    if (!transform_.DecodeTransformData(buffer)) {
      return false;
    }
    return true;
  }

  const PointAttribute *GetAttribute() const override { return attribute(); }

  // Returns the number of parent attributes that are needed for the prediction.
  int GetNumParentAttributes() const override { return 0; }

  // Returns the type of each of the parent attribute.
  GeometryAttribute::Type GetParentAttributeType(int) const override {
    return GeometryAttribute::INVALID;
}

// Sets the required parent attribute.
bool SetParentAttribute(const PointAttribute*) override {
    return false;
}

bool AreCorrectionsPositive() override {
    return transform_.AreCorrectionsPositive();
}

PredictionSchemeTransformType GetTransformType() const override {
    return transform_.GetType();
}

protected:
inline const PointAttribute *attribute() const { return attribute_; }
inline const Transform &transform() const { return transform_; }
inline Transform &transform() { return transform_; }

private:
        const PointAttribute *attribute_;
Transform transform_;
};

        }  // namespace draco

        #endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODER_H_

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.attributes.PointAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class PredictionSchemeDecoder<DataT, CorrT>
        implements PredictionSchemeTypedDecoderInterface<DataT, CorrT> {

    private final PointAttribute attribute;
    private PredictionSchemeDecodingTransform<DataT, CorrT> transform;

    @Override public DataNumberType<DataT, ?> getDataType() { return transform.getDataType(); }
    @Override public DataNumberType<CorrT, ?> getCorrType() { return transform.getCorrType(); }

    @Override
    public Status decodePredictionData(DecoderBuffer buffer) {
        return transform.decodeTransformData(buffer);
    }

    @Override
    public int getNumParentAttributes() {
        return 0;
    }

    @Override
    public PointAttribute.Type getParentAttributeType(int i) {
        return GeometryAttribute.Type.INVALID;
    }

    @Override
    public Status setParentAttribute(PointAttribute att) {
        return Status.unsupportedFeature("Parent attributes are not supported");
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
