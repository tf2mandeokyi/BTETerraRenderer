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
#ifndef DRACO_COMPRESSION_ENCODE_H_
#define DRACO_COMPRESSION_ENCODE_H_

#include "draco/compression/config/compression_shared.h"
#include "draco/compression/config/encoder_options.h"
#include "draco/compression/encode_base.h"
#include "draco/core/encoder_buffer.h"
#include "draco/core/status.h"
#include "draco/mesh/mesh.h"

namespace draco {

// Basic helper class for encoding geometry using the Draco compression library.
// The class provides various methods that can be used to control several common
// options used during the encoding, such as the number of quantization bits for
// a given attribute. All these options are defined per attribute type, i.e.,
// if there are more attributes of the same type (such as multiple texture
// coordinate attributes), the same options are going to be used for all of the
// attributes of this type. If different attributes of the same type need to
// use different options, use ExpertEncoder in expert_encode.h.
class Encoder
    : public EncoderBase<EncoderOptionsBase<GeometryAttribute::Type>> {
 public:
  typedef EncoderBase<EncoderOptionsBase<GeometryAttribute::Type>> Base;

  Encoder();
  virtual ~Encoder() {}

  // Encodes a point cloud to the provided buffer.
  virtual Status EncodePointCloudToBuffer(const PointCloud &pc,
                                          EncoderBuffer *out_buffer);

  // Encodes a mesh to the provided buffer.
  virtual Status EncodeMeshToBuffer(const Mesh &m, EncoderBuffer *out_buffer);

  // Set encoder options used during the geometry encoding. Note that this call
  // overwrites any modifications to the options done with the functions below,
  // i.e., it resets the encoder.
  void Reset(const EncoderOptionsBase<GeometryAttribute::Type> &options);
  void Reset();

  // Sets the desired encoding and decoding speed for the given options.
  //
  //  0 = slowest speed, but the best compression.
  // 10 = fastest, but the worst compression.
  // -1 = undefined.
  //
  // Note that both speed options affect the encoder choice of used methods and
  // algorithms. For example, a requirement for fast decoding may prevent the
  // encoder from using the best compression methods even if the encoding speed
  // is set to 0. In general, the faster of the two options limits the choice of
  // features that can be used by the encoder. Additionally, setting
  // |decoding_speed| to be faster than the |encoding_speed| may allow the
  // encoder to choose the optimal method out of the available features for the
  // given |decoding_speed|.
  void SetSpeedOptions(int encoding_speed, int decoding_speed);

  // Sets the quantization compression options for a named attribute. The
  // attribute values will be quantized in a box defined by the maximum extent
  // of the attribute values. I.e., the actual precision of this option depends
  // on the scale of the attribute values.
  void SetAttributeQuantization(GeometryAttribute::Type type,
                                int quantization_bits);

  // Sets the explicit quantization compression for a named attribute. The
  // attribute values will be quantized in a coordinate system defined by the
  // provided origin and range (the input values should be within interval:
  // <origin, origin + range>).
  void SetAttributeExplicitQuantization(GeometryAttribute::Type type,
                                        int quantization_bits, int num_dims,
                                        const float *origin, float range);

  // Sets the desired prediction method for a given attribute. By default,
  // prediction scheme is selected automatically by the encoder using other
  // provided options (such as speed) and input geometry type (mesh, point
  // cloud). This function should be called only when a specific prediction is
  // preferred (e.g., when it is known that the encoder would select a less
  // optimal prediction for the given input data).
  //
  // |prediction_scheme_method| should be one of the entries defined in
  // compression/config/compression_shared.h :
  //
  //   PREDICTION_NONE - use no prediction.
  //   PREDICTION_DIFFERENCE - delta coding
  //   MESH_PREDICTION_PARALLELOGRAM - parallelogram prediction for meshes.
  //   MESH_PREDICTION_CONSTRAINED_PARALLELOGRAM
  //      - better and more costly version of the parallelogram prediction.
  //   MESH_PREDICTION_TEX_COORDS_PORTABLE
  //      - specialized predictor for tex coordinates.
  //   MESH_PREDICTION_GEOMETRIC_NORMAL
  //      - specialized predictor for normal coordinates.
  //
  // Note that in case the desired prediction cannot be used, the default
  // prediction will be automatically used instead.
  Status SetAttributePredictionScheme(GeometryAttribute::Type type,
                                      int prediction_scheme_method);

  // Sets the desired encoding method for a given geometry. By default, encoding
  // method is selected based on the properties of the input geometry and based
  // on the other options selected in the used EncoderOptions (such as desired
  // encoding and decoding speed). This function should be called only when a
  // specific method is required.
  //
  // |encoding_method| can be one of the values defined in
  // compression/config/compression_shared.h based on the type of the input
  // geometry that is going to be encoded. For point clouds, allowed entries are
  //   POINT_CLOUD_SEQUENTIAL_ENCODING
  //   POINT_CLOUD_KD_TREE_ENCODING
  //
  // For meshes the input can be
  //   MESH_SEQUENTIAL_ENCODING
  //   MESH_EDGEBREAKER_ENCODING
  //
  // If the selected method cannot be used for the given input, the subsequent
  // call of EncodePointCloudToBuffer or EncodeMeshToBuffer is going to fail.
  void SetEncodingMethod(int encoding_method);

  // Creates encoder options for the expert encoder used during the actual
  // encoding.
  EncoderOptions CreateExpertEncoderOptions(const PointCloud &pc) const;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_ENCODE_H_

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
#include "draco/compression/encode.h"

#include "draco/compression/expert_encode.h"

namespace draco {

Encoder::Encoder() {}

Status Encoder::EncodePointCloudToBuffer(const PointCloud &pc,
                                         EncoderBuffer *out_buffer) {
  ExpertEncoder encoder(pc);
  encoder.Reset(CreateExpertEncoderOptions(pc));
  return encoder.EncodeToBuffer(out_buffer);
}

Status Encoder::EncodeMeshToBuffer(const Mesh &m, EncoderBuffer *out_buffer) {
  ExpertEncoder encoder(m);
  encoder.Reset(CreateExpertEncoderOptions(m));
  DRACO_RETURN_IF_ERROR(encoder.EncodeToBuffer(out_buffer));
  set_num_encoded_points(encoder.num_encoded_points());
  set_num_encoded_faces(encoder.num_encoded_faces());
  return OkStatus();
}

EncoderOptions Encoder::CreateExpertEncoderOptions(const PointCloud &pc) const {
  EncoderOptions ret_options = EncoderOptions::CreateEmptyOptions();
  ret_options.SetGlobalOptions(options().GetGlobalOptions());
  ret_options.SetFeatureOptions(options().GetFeaturelOptions());
  // Convert type-based attribute options to specific attributes in the provided
  // point cloud.
  for (int i = 0; i < pc.num_attributes(); ++i) {
    const Options *att_options =
        options().FindAttributeOptions(pc.attribute(i)->attribute_type());
    if (att_options) {
      ret_options.SetAttributeOptions(i, *att_options);
    }
  }
  return ret_options;
}

void Encoder::Reset(
    const EncoderOptionsBase<GeometryAttribute::Type> &options) {
  Base::Reset(options);
}

void Encoder::Reset() { Base::Reset(); }

void Encoder::SetSpeedOptions(int encoding_speed, int decoding_speed) {
  Base::SetSpeedOptions(encoding_speed, decoding_speed);
}

void Encoder::SetAttributeQuantization(GeometryAttribute::Type type,
                                       int quantization_bits) {
  options().SetAttributeInt(type, "quantization_bits", quantization_bits);
}

void Encoder::SetAttributeExplicitQuantization(GeometryAttribute::Type type,
                                               int quantization_bits,
                                               int num_dims,
                                               const float *origin,
                                               float range) {
  options().SetAttributeInt(type, "quantization_bits", quantization_bits);
  options().SetAttributeVector(type, "quantization_origin", num_dims, origin);
  options().SetAttributeFloat(type, "quantization_range", range);
}

void Encoder::SetEncodingMethod(int encoding_method) {
  Base::SetEncodingMethod(encoding_method);
}

Status Encoder::SetAttributePredictionScheme(GeometryAttribute::Type type,
                                             int prediction_scheme_method) {
  Status status = CheckPredictionScheme(type, prediction_scheme_method);
  if (!status.ok()) {
    return status;
  }
  options().SetAttributeInt(type, "prediction_scheme",
                            prediction_scheme_method);
  return status;
}

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptionsBase;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

public class DracoEncoder extends DracoEncoderBase<EncoderOptionsBase<GeometryAttribute.Type>> {

    public Status encodePointCloudToBuffer(PointCloud pc, EncoderBuffer outBuffer) {

    }

    public Status encodeMeshToBuffer(Mesh m, EncoderBuffer outBuffer) {

    }

    public void reset(EncoderOptionsBase<GeometryAttribute.Type> options) {

    }

    public void reset() {

    }

    public void setSpeedOptions(int encodingSpeed, int decodingSpeed) {

    }

    public void setAttributeQuantization(GeometryAttribute.Type type, int quantizationBits) {

    }

    public void setAttributeExplicitQuantization(GeometryAttribute.Type type, int quantizationBits, int numDims,
                                                 CppVector<Float> origin, float range) {

    }

    public Status setAttributePredictionScheme(GeometryAttribute.Type type, int predictionSchemeMethod) {

    }

    public void setEncodingMethod(int encodingMethod) {

    }

    public EncoderOptions createExpertEncoderOptions(PointCloud pc) {

    }

    @Override protected EncoderOptionsBase<GeometryAttribute.Type> createDefaultOptions() {
        return EncoderOptionsBase.createDefaultOptions();
    }
}
