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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODING_TRANSFORM_H_
#define DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODING_TRANSFORM_H_

#include "draco/compression/config/compression_shared.h"
#include "draco/core/decoder_buffer.h"

namespace draco {

// PredictionSchemeDecodingTransform is used to transform predicted values and
// correction values into the final original attribute values.
// DataTypeT is the data type of predicted values.
// CorrTypeT is the data type used for storing corrected values. It allows
// transforms to store corrections into a different type or format compared to
// the predicted data.
template <typename DataTypeT, typename CorrTypeT>
class PredictionSchemeDecodingTransform {
 public:
  typedef CorrTypeT CorrType;
  PredictionSchemeDecodingTransform() : num_components_(0) {}

  void Init(int num_components) { num_components_ = num_components; }

  // Computes the original value from the input predicted value and the decoded
  // corrections. The default implementation is equal to std:plus.
  inline void ComputeOriginalValue(const DataTypeT *predicted_vals,
                                   const CorrTypeT *corr_vals,
                                   DataTypeT *out_original_vals) const {
    static_assert(std::is_same<DataTypeT, CorrTypeT>::value,
                  "For the default prediction transform, correction and input "
                  "data must be of the same type.");
    for (int i = 0; i < num_components_; ++i) {
      out_original_vals[i] = predicted_vals[i] + corr_vals[i];
    }
  }

  // Decodes any transform specific data. Called before Init() method.
  bool DecodeTransformData(DecoderBuffer*) { return true; }

// Should return true if all corrected values are guaranteed to be positive.
bool AreCorrectionsPositive() const { return false; }

protected:
int num_components() const { return num_components_; }

private:
int num_components_;
};

        }  // namespace draco

        #endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODING_TRANSFORM_H_

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.draco.compression.config.PredictionSchemeTransformType;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

public interface PredictionSchemeDecodingTransform<DataT, CorrT> {

    void init(int numComponents);
    int getNumComponents();

    DataNumberType<DataT, ?> getDataType();
    DataNumberType<CorrT, ?> getCorrType();

    default void computeOriginalValue(CppVector<DataT> predictedVals, CppVector<CorrT> corrVals,
                                      CppVector<DataT> outOriginalVals) {
        DataNumberType<DataT, ?> dataType = this.getDataType();
        DataNumberType<CorrT, ?> corrType = this.getCorrType();
        if(!dataType.equals(corrType)) {
            throw new IllegalArgumentException("For the default prediction transform, correction and input " +
                    "data must be of the same type.");
        }
        for (int i = 0; i < getNumComponents(); ++i) {
            outOriginalVals.set(i, dataType.add(predictedVals.get(i), corrType, corrVals.get(i)));
        }
    }

    default Status decodeTransformData(DecoderBuffer buffer) { return Status.ok(); }
    default boolean areCorrectionsPositive() { return false; }
    PredictionSchemeTransformType getType();
    default int getQuantizationBits() {
        throw new UnsupportedOperationException("This transform does not support quantization bits");
    }
}
