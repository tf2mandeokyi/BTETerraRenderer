/*
// Copyright 2017 The Draco Authors.
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
#ifndef DRACO_COMPRESSION_EXPERT_ENCODE_H_
#define DRACO_COMPRESSION_EXPERT_ENCODE_H_

#include "draco/compression/config/compression_shared.h"
#include "draco/compression/config/encoder_options.h"
#include "draco/compression/encode_base.h"
#include "draco/core/encoder_buffer.h"
#include "draco/core/status.h"
#include "draco/mesh/mesh.h"

namespace draco {

// Advanced helper class for encoding geometry using the Draco compression
// library. Unlike the basic Encoder (encode.h), this class allows users to
// specify options for each attribute individually using provided attribute ids.
// The drawback of this encoder is that it can be used to encode only one model
// at a time, and for each new model the options need to be set again,
class ExpertEncoder : public EncoderBase<EncoderOptions> {
 public:
  typedef EncoderBase<EncoderOptions> Base;
  typedef EncoderOptions OptionsType;

  explicit ExpertEncoder(const PointCloud &point_cloud);
  explicit ExpertEncoder(const Mesh &mesh);

  // Encodes the geometry provided in the constructor to the target buffer.
  Status EncodeToBuffer(EncoderBuffer *out_buffer);

  // Set encoder options used during the geometry encoding. Note that this call
  // overwrites any modifications to the options done with the functions below.
  void Reset(const EncoderOptions &options);
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

  // Sets the quantization compression options for a specific attribute. The
  // attribute values will be quantized in a box defined by the maximum extent
  // of the attribute values. I.e., the actual precision of this option depends
  // on the scale of the attribute values.
  void SetAttributeQuantization(int32_t attribute_id, int quantization_bits);

  // Sets the explicit quantization compression for a named attribute. The
  // attribute values will be quantized in a coordinate system defined by the
  // provided origin and range (the input values should be within interval:
  // <origin, origin + range>).
  void SetAttributeExplicitQuantization(int32_t attribute_id,
                                        int quantization_bits, int num_dims,
                                        const float *origin, float range);

  // Enables/disables built in entropy coding of attribute values. Disabling
  // this option may be useful to improve the performance when third party
  // compression is used on top of the Draco compression. Default: [true].
  void SetUseBuiltInAttributeCompression(bool enabled);

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

  // Sets the desired encoding submethod, only for MESH_EDGEBREAKER_ENCODING.
  // Valid values for |encoding_submethod| are:
  //   MESH_EDGEBREAKER_STANDARD_ENCODING
  //   MESH_EDGEBREAKER_VALENCE_ENCODING
  // see also compression/config/compression_shared.h.
  void SetEncodingSubmethod(int encoding_submethod);

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
  Status SetAttributePredictionScheme(int32_t attribute_id,
                                      int prediction_scheme_method);

#ifdef DRACO_TRANSCODER_SUPPORTED
  // Applies grid quantization to position attribute in point cloud |pc| at
  // |attribute_index| with a given grid |spacing|.
  Status SetAttributeGridQuantization(const PointCloud &pc, int attribute_index,
                                      float spacing);
#endif  // DRACO_TRANSCODER_SUPPORTED

 private:
  Status EncodePointCloudToBuffer(const PointCloud &pc,
                                  EncoderBuffer *out_buffer);

  Status EncodeMeshToBuffer(const Mesh &m, EncoderBuffer *out_buffer);

#ifdef DRACO_TRANSCODER_SUPPORTED
  // Applies compression options stored in |pc|.
  Status ApplyCompressionOptions(const PointCloud &pc);
  Status ApplyGridQuantization(const PointCloud &pc, int attribute_index);
#endif  // DRACO_TRANSCODER_SUPPORTED

  const PointCloud *point_cloud_;
  const Mesh *mesh_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_EXPERT_ENCODE_H_

 */

/*
// Copyright 2017 The Draco Authors.
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
#include "draco/compression/expert_encode.h"

#include <iostream>
#include <memory>
#include <ostream>
#include <string>
#include <utility>

#include "draco/compression/mesh/mesh_edgebreaker_encoder.h"
#include "draco/compression/mesh/mesh_sequential_encoder.h"
#ifdef DRACO_POINT_CLOUD_COMPRESSION_SUPPORTED
#include "draco/compression/point_cloud/point_cloud_kd_tree_encoder.h"
#include "draco/compression/point_cloud/point_cloud_sequential_encoder.h"
#endif

#ifdef DRACO_TRANSCODER_SUPPORTED
#include "draco/core/bit_utils.h"
#endif
namespace draco {

ExpertEncoder::ExpertEncoder(const PointCloud &point_cloud)
    : point_cloud_(&point_cloud), mesh_(nullptr) {}

ExpertEncoder::ExpertEncoder(const Mesh &mesh)
    : point_cloud_(&mesh), mesh_(&mesh) {}

Status ExpertEncoder::EncodeToBuffer(EncoderBuffer *out_buffer) {
  if (point_cloud_ == nullptr) {
    return Status(Status::DRACO_ERROR, "Invalid input geometry.");
  }
  if (mesh_ == nullptr) {
    return EncodePointCloudToBuffer(*point_cloud_, out_buffer);
  }
  return EncodeMeshToBuffer(*mesh_, out_buffer);
}

Status ExpertEncoder::EncodePointCloudToBuffer(const PointCloud &pc,
                                               EncoderBuffer *out_buffer) {
#ifdef DRACO_POINT_CLOUD_COMPRESSION_SUPPORTED
#ifdef DRACO_TRANSCODER_SUPPORTED
  // Apply DracoCompressionOptions associated with the point cloud.
  DRACO_RETURN_IF_ERROR(ApplyCompressionOptions(pc));
#endif  // DRACO_TRANSCODER_SUPPORTED

  std::unique_ptr<PointCloudEncoder> encoder;
  const int encoding_method = options().GetGlobalInt("encoding_method", -1);

  if (encoding_method == POINT_CLOUD_SEQUENTIAL_ENCODING) {
    // Use sequential encoding if requested.
    encoder.reset(new PointCloudSequentialEncoder());
  } else if (encoding_method == -1 && options().GetSpeed() == 10) {
    // Use sequential encoding if speed is at max.
    encoder.reset(new PointCloudSequentialEncoder());
  } else {
    // Speed < 10, use POINT_CLOUD_KD_TREE_ENCODING if possible.
    bool kd_tree_possible = true;
    // Kd-Tree encoder can be currently used only when the following conditions
    // are satisfied for all attributes:
    //     -data type is float32 and quantization is enabled, OR
    //     -data type is uint32, uint16, uint8 or int32, int16, int8
    for (int i = 0; i < pc.num_attributes(); ++i) {
      const PointAttribute *const att = pc.attribute(i);
      if (kd_tree_possible && att->data_type() != DT_FLOAT32 &&
          att->data_type() != DT_UINT32 && att->data_type() != DT_UINT16 &&
          att->data_type() != DT_UINT8 && att->data_type() != DT_INT32 &&
          att->data_type() != DT_INT16 && att->data_type() != DT_INT8) {
        kd_tree_possible = false;
      }
      if (kd_tree_possible && att->data_type() == DT_FLOAT32 &&
          options().GetAttributeInt(i, "quantization_bits", -1) <= 0) {
        kd_tree_possible = false;  // Quantization not enabled.
      }
      if (!kd_tree_possible) {
        break;
      }
    }

    if (kd_tree_possible) {
      // Create kD-tree encoder (all checks passed).
      encoder.reset(new PointCloudKdTreeEncoder());
    } else if (encoding_method == POINT_CLOUD_KD_TREE_ENCODING) {
      // Encoding method was explicitly specified but we cannot use it for
      // the given input (some of the checks above failed).
      return Status(Status::DRACO_ERROR, "Invalid encoding method.");
    }
  }
  if (!encoder) {
    // Default choice.
    encoder.reset(new PointCloudSequentialEncoder());
  }
  encoder->SetPointCloud(pc);
  DRACO_RETURN_IF_ERROR(encoder->Encode(options(), out_buffer));

  set_num_encoded_points(encoder->num_encoded_points());
  set_num_encoded_faces(0);
  return OkStatus();
#else
  return Status(Status::DRACO_ERROR, "Point cloud encoding is not enabled.");
#endif
}

Status ExpertEncoder::EncodeMeshToBuffer(const Mesh &m,
                                         EncoderBuffer *out_buffer) {
#ifdef DRACO_TRANSCODER_SUPPORTED
  // Apply DracoCompressionOptions associated with the mesh.
  DRACO_RETURN_IF_ERROR(ApplyCompressionOptions(m));
#endif  // DRACO_TRANSCODER_SUPPORTED

  std::unique_ptr<MeshEncoder> encoder;
  // Select the encoding method only based on the provided options.
  int encoding_method = options().GetGlobalInt("encoding_method", -1);
  if (encoding_method == -1) {
    // For now select the edgebreaker for all options expect of speed 10
    if (options().GetSpeed() == 10) {
      encoding_method = MESH_SEQUENTIAL_ENCODING;
    } else {
      encoding_method = MESH_EDGEBREAKER_ENCODING;
    }
  }
  if (encoding_method == MESH_EDGEBREAKER_ENCODING) {
    encoder = std::unique_ptr<MeshEncoder>(new MeshEdgebreakerEncoder());
  } else {
    encoder = std::unique_ptr<MeshEncoder>(new MeshSequentialEncoder());
  }
  encoder->SetMesh(m);

  DRACO_RETURN_IF_ERROR(encoder->Encode(options(), out_buffer));

  set_num_encoded_points(encoder->num_encoded_points());
  set_num_encoded_faces(encoder->num_encoded_faces());
  return OkStatus();
}

void ExpertEncoder::Reset(const EncoderOptions &options) {
  Base::Reset(options);
}

void ExpertEncoder::Reset() { Base::Reset(); }

void ExpertEncoder::SetSpeedOptions(int encoding_speed, int decoding_speed) {
  Base::SetSpeedOptions(encoding_speed, decoding_speed);
}

void ExpertEncoder::SetAttributeQuantization(int32_t attribute_id,
                                             int quantization_bits) {
  options().SetAttributeInt(attribute_id, "quantization_bits",
                            quantization_bits);
}

void ExpertEncoder::SetAttributeExplicitQuantization(int32_t attribute_id,
                                                     int quantization_bits,
                                                     int num_dims,
                                                     const float *origin,
                                                     float range) {
  options().SetAttributeInt(attribute_id, "quantization_bits",
                            quantization_bits);
  options().SetAttributeVector(attribute_id, "quantization_origin", num_dims,
                               origin);
  options().SetAttributeFloat(attribute_id, "quantization_range", range);
}

void ExpertEncoder::SetUseBuiltInAttributeCompression(bool enabled) {
  options().SetGlobalBool("use_built_in_attribute_compression", enabled);
}

void ExpertEncoder::SetEncodingMethod(int encoding_method) {
  Base::SetEncodingMethod(encoding_method);
}

void ExpertEncoder::SetEncodingSubmethod(int encoding_submethod) {
  Base::SetEncodingSubmethod(encoding_submethod);
}

Status ExpertEncoder::SetAttributePredictionScheme(
    int32_t attribute_id, int prediction_scheme_method) {
  auto att = point_cloud_->attribute(attribute_id);
  auto att_type = att->attribute_type();
  const Status status =
      CheckPredictionScheme(att_type, prediction_scheme_method);
  if (!status.ok()) {
    return status;
  }
  options().SetAttributeInt(attribute_id, "prediction_scheme",
                            prediction_scheme_method);
  return status;
}

#ifdef DRACO_TRANSCODER_SUPPORTED
Status ExpertEncoder::ApplyCompressionOptions(const PointCloud &pc) {
  if (!pc.IsCompressionEnabled()) {
    return OkStatus();
  }
  const auto &compression_options = pc.GetCompressionOptions();

  // Set any encoder options that haven't been explicitly set by users (don't
  // override existing options).
  if (!options().IsSpeedSet()) {
    options().SetSpeed(10 - compression_options.compression_level,
                       10 - compression_options.compression_level);
  }

  for (int ai = 0; ai < pc.num_attributes(); ++ai) {
    if (options().IsAttributeOptionSet(ai, "quantization_bits")) {
      continue;  // Don't override options that have been set.
    }
    int quantization_bits = 0;
    const auto type = pc.attribute(ai)->attribute_type();
    switch (type) {
      case GeometryAttribute::POSITION:
        if (compression_options.quantization_position
                .AreQuantizationBitsDefined()) {
          quantization_bits =
              compression_options.quantization_position.quantization_bits();
        } else {
          DRACO_RETURN_IF_ERROR(ApplyGridQuantization(pc, ai));
        }
        break;
      case GeometryAttribute::TEX_COORD:
        quantization_bits = compression_options.quantization_bits_tex_coord;
        break;
      case GeometryAttribute::NORMAL:
        quantization_bits = compression_options.quantization_bits_normal;
        break;
      case GeometryAttribute::COLOR:
        quantization_bits = compression_options.quantization_bits_color;
        break;
      case GeometryAttribute::TANGENT:
        quantization_bits = compression_options.quantization_bits_tangent;
        break;
      case GeometryAttribute::WEIGHTS:
        quantization_bits = compression_options.quantization_bits_weight;
        break;
      case GeometryAttribute::GENERIC:
        quantization_bits = compression_options.quantization_bits_generic;
        break;
      default:
        break;
    }
    if (quantization_bits > 0) {
      options().SetAttributeInt(ai, "quantization_bits", quantization_bits);
    }
  }
  return OkStatus();
}

Status ExpertEncoder::ApplyGridQuantization(const PointCloud &pc,
                                            int attribute_index) {
  const auto compression_options = pc.GetCompressionOptions();
  const float spacing = compression_options.quantization_position.spacing();
  return SetAttributeGridQuantization(pc, attribute_index, spacing);
}

Status ExpertEncoder::SetAttributeGridQuantization(const PointCloud &pc,
                                                   int attribute_index,
                                                   float spacing) {
  const auto *const att = pc.attribute(attribute_index);
  if (att->attribute_type() != GeometryAttribute::POSITION) {
    return ErrorStatus(
        "Invalid attribute type: Grid quantization is currently supported only "
        "for positions.");
  }
  if (att->num_components() != 3) {
    return ErrorStatus(
        "Invalid number of components: Grid quantization is currently "
        "supported only for 3D positions.");
  }
  // Compute quantization properties based on the grid spacing.
  const auto &bbox = pc.ComputeBoundingBox();
  // Snap min and max points of the |bbox| to the quantization grid vertices.
  Vector3f min_pos;
  int num_values = 0;  // Number of values that we need to encode.
  for (int c = 0; c < 3; ++c) {
    // Min / max position on grid vertices in grid coordinates.
    const float min_grid_pos = floor(bbox.GetMinPoint()[c] / spacing);
    const float max_grid_pos = ceil(bbox.GetMaxPoint()[c] / spacing);

    // Min pos on grid vertex in mesh coordinates.
    min_pos[c] = min_grid_pos * spacing;

    const float component_num_values =
        static_cast<int>(max_grid_pos) - static_cast<int>(min_grid_pos) + 1;
    if (component_num_values > num_values) {
      num_values = component_num_values;
    }
  }
  // Now compute the number of bits needed to encode |num_values|.
  int bits = MostSignificantBit(num_values);
  if ((1 << bits) < num_values) {
    // If the |num_values| is larger than number of values representable by
    // |bits|, we need to use one more bit. This will be almost always true
    // unless |num_values| was equal to 1 << |bits|.
    bits++;
  }
  // Compute the range in mesh coordinates that matches the quantization bits.
  // Note there are n-1 intervals between the |n| quantization values.
  const float range = ((1 << bits) - 1) * spacing;
  SetAttributeExplicitQuantization(attribute_index, bits, 3, min_pos.data(),
                                   range);
  return OkStatus();
}
#endif  // DRACO_TRANSCODER_SUPPORTED

}  // namespace draco

 */

package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.draco.compression.config.EncoderOptions;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

public class DracoExpertEncoder extends DracoEncoderBase<EncoderOptions> {

    private PointCloud pointCloud;
    private Mesh mesh;

    public DracoExpertEncoder(PointCloud pointCloud) {
        this.pointCloud = pointCloud;
        this.mesh = null;
    }

    public DracoExpertEncoder(Mesh mesh) {
        this.pointCloud = mesh;
        this.mesh = mesh;
    }

    public Status encodeToBuffer(EncoderBuffer outBuffer) {
        if (pointCloud == null) {
            return Status.dracoError("Invalid input geometry.");
        }
        if (mesh == null) {
            return encodePointCloudToBuffer(pointCloud, outBuffer);
        }
        return encodeMeshToBuffer(mesh, outBuffer);
    }

    public void reset(EncoderOptions options) {

    }

    public void reset() {

    }

    public void setSpeedOptions(int encodingSpeed, int decodingSpeed) {

    }

    public void setAttributeQuantization(int attributeId, int quantizationBits) {

    }

    public void setAttributeExplicitQuantization(int attributeId, int quantizationBits, int numDims,
                                                 CppVector<Float> origin, float range) {

    }

    public void setUseBuiltInAttributeCompression(boolean enabled) {

    }

    public void setEncodingMethod(int encodingMethod) {

    }

    public void setEncodingSubmethod(int encodingSubmethod) {

    }

    public Status setAttributePredictionScheme(int attributeId, int predictionSchemeMethod) {

    }

    private Status encodePointCloudToBuffer(PointCloud pc, EncoderBuffer outBuffer) {
        // TODO: Implement this
        return Status.dracoError("Point cloud encoding is not enabled.");
    }

    private Status encodeMeshToBuffer(Mesh m, EncoderBuffer outBuffer) {

    }

    @Override protected EncoderOptions createDefaultOptions() {
        return EncoderOptions.createDefaultOptions();
    }
}
