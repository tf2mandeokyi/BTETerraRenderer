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
#ifndef DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODER_INTERFACE_H_
#define DRACO_COMPRESSION_ATTRIBUTES_PREDICTION_SCHEMES_PREDICTION_SCHEME_DECODER_INTERFACE_H_

#include "draco/compression/attributes/prediction_schemes/prediction_scheme_interface.h"
#include "draco/core/decoder_buffer.h"

// Prediction schemes can be used during encoding and decoding of attributes
// to predict attribute values based on the previously encoded/decoded data.
// See prediction_scheme.h for more details.
namespace draco {

// Abstract interface for all prediction schemes used during attribute encoding.
class PredictionSchemeDecoderInterface : public PredictionSchemeInterface {
 public:
  // Method that can be used to decode any prediction scheme specific data
  // from the input buffer.
  virtual bool DecodePredictionData(DecoderBuffer *buffer) = 0;
};

 */

package com.mndk.bteterrarenderer.draco.compression.attributes.scheme;

import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;

public interface PredictionSchemeDecoderInterface extends PredictionSchemeInterface {
    Status decodePredictionData(DecoderBuffer buffer);
}
