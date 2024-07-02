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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_WRAP_DECODING_TRANSFORM_H_
#define DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_WRAP_DECODING_TRANSFORM_H_

#include "draco/compression/attributes/prediction_schemes/prediction_scheme_wrap_transform_base.h"
#include "draco/core/decoder_buffer.h"

namespace draco {

// PredictionSchemeWrapDecodingTransform unwraps values encoded with the
// PredictionSchemeWrapEncodingTransform.
// See prediction_scheme_wrap_transform_base.h for more details about the
// method.
template <typename DataTypeT, typename CorrTypeT = DataTypeT>
class PredictionSchemeWrapDecodingTransform
    : public PredictionSchemeWrapTransformBase<DataTypeT> {
 public:
  typedef CorrTypeT CorrType;
  PredictionSchemeWrapDecodingTransform() {}

  // Computes the original value from the input predicted value and the decoded
  // corrections. Values out of the bounds of the input values are unwrapped.
  inline void ComputeOriginalValue(const DataTypeT *predicted_vals,
                                   const CorrTypeT *corr_vals,
                                   DataTypeT *out_original_vals) const {
    // For now we assume both |DataTypeT| and |CorrTypeT| are equal.
    static_assert(std::is_same<DataTypeT, CorrTypeT>::value,
                  "Predictions and corrections must have the same type.");

    // The only valid implementation right now is for int32_t.
    static_assert(std::is_same<DataTypeT, int32_t>::value,
                  "Only int32_t is supported for predicted values.");

    predicted_vals = this->ClampPredictedValue(predicted_vals);

    // Perform the wrapping using unsigned coordinates to avoid potential signed
    // integer overflows caused by malformed input.
    const uint32_t *const uint_predicted_vals =
        reinterpret_cast<const uint32_t *>(predicted_vals);
    const uint32_t *const uint_corr_vals =
        reinterpret_cast<const uint32_t *>(corr_vals);
    for (int i = 0; i < this->num_components(); ++i) {
      out_original_vals[i] =
          static_cast<DataTypeT>(uint_predicted_vals[i] + uint_corr_vals[i]);
      if (out_original_vals[i] > this->max_value()) {
        out_original_vals[i] -= this->max_dif();
      } else if (out_original_vals[i] < this->min_value()) {
        out_original_vals[i] += this->max_dif();
      }
    }
  }

  bool DecodeTransformData(DecoderBuffer *buffer) {
    DataTypeT min_value, max_value;
    if (!buffer->Decode(&min_value)) {
      return false;
    }
    if (!buffer->Decode(&max_value)) {
      return false;
    }
    if (min_value > max_value) {
      return false;
    }
    this->set_min_value(min_value);
    this->set_max_value(max_value);
    if (!this->InitCorrectionBounds()) {
      return false;
    }
    return true;
  }
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_WRAP_DECODING_TRANSFORM_H_

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;

@Getter
public class PredictionSchemeWrapDecodingTransform<DataT, CorrT>
        extends PredictionSchemeWrapTransformBase<DataT>
        implements PredictionSchemeDecodingTransform<DataT, CorrT> {

    private final DataNumberType<CorrT, ?> corrType;

    public PredictionSchemeWrapDecodingTransform(DataNumberType<DataT, ?> dataType, DataNumberType<CorrT, ?> corrType) {
        super(dataType);
        this.corrType = corrType;
    }

    @Override
    public void computeOriginalValue(CppVector<DataT> predictedVals, CppVector<CorrT> corrVals,
                                     CppVector<DataT> outOriginalVals) {
        DataNumberType<DataT, ?> dataType = this.getDataType();
        if(!dataType.equals(corrType)) {
            throw new IllegalArgumentException("Predictions and corrections must have the same type.");
        }

        // The only valid implementation right now is for int32_t.
        // ...bruh
        if(!dataType.equals(DataType.int32())) {
            throw new IllegalArgumentException("Only int32_t is supported for predicted values.");
        }

        predictedVals = this.clampPredictedValue(predictedVals);

        // Perform the wrapping using unsigned coordinates to avoid potential signed
        // integer overflows caused by malformed input.
        CppVector<UInt> uintPredictedVals = predictedVals.cast(dataType::toUInt, dataType::from);
        CppVector<UInt> uintCorrVals = corrVals.cast(corrType::toUInt, corrType::from);
        for (int i = 0; i < this.getNumComponents(); ++i) {
            DataT outOriginalVal = dataType.from(uintPredictedVals.get(i).add(uintCorrVals.get(i)));
            if (dataType.gt(outOriginalVal, this.getMaxValue())) {
                outOriginalVal = dataType.sub(outOriginalVal, this.getMaxDif());
            } else if (dataType.lt(outOriginalVal, this.getMinValue())) {
                outOriginalVal = dataType.add(outOriginalVal, this.getMaxDif());
            }
            outOriginalVals.set(i, outOriginalVal);
        }
    }

    @Override
    public Status decodeTransformData(DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        DataNumberType<DataT, ?> dataType = this.getDataType();
        AtomicReference<DataT> minValue = new AtomicReference<>();
        AtomicReference<DataT> maxValue = new AtomicReference<>();
        if(buffer.decode(dataType, minValue::set).isError(chain)) return chain.get();
        if(buffer.decode(dataType, maxValue::set).isError(chain)) return chain.get();
        if(dataType.gt(minValue.get(), maxValue.get())) {
            return Status.ioError("Min value is greater than max value");
        }
        this.setMinValue(minValue.get());
        this.setMaxValue(maxValue.get());
        return this.initCorrectionBounds();
    }
}
