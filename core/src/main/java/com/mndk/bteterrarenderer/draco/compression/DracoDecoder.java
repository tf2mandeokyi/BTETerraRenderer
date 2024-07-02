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
#ifndef DRACO_COMPRESSION_DECODE_H_
#define DRACO_COMPRESSION_DECODE_H_

#include "draco/compression/config/compression_shared.h"
#include "draco/compression/config/decoder_options.h"
#include "draco/core/decoder_buffer.h"
#include "draco/core/status_or.h"
#include "draco/draco_features.h"
#include "draco/mesh/mesh.h"

namespace draco {

// Class responsible for decoding of meshes and point clouds that were
// compressed by a Draco encoder.
class Decoder {
 public:
  // Returns the geometry type encoded in the input |in_buffer|.
  // The return value is one of POINT_CLOUD, MESH or INVALID_GEOMETRY in case
  // the input data is invalid.
  // The decoded geometry type can be used to choose an appropriate decoding
  // function for a given geometry type (see below).
  static StatusOr<EncodedGeometryType> GetEncodedGeometryType(
      DecoderBuffer *in_buffer);

  // Decodes point cloud from the provided buffer. The buffer must be filled
  // with data that was encoded with either the EncodePointCloudToBuffer or
  // EncodeMeshToBuffer methods in encode.h. In case the input buffer contains
  // mesh, the returned instance can be down-casted to Mesh.
  StatusOr<std::unique_ptr<PointCloud>> DecodePointCloudFromBuffer(
      DecoderBuffer *in_buffer);

  // Decodes a triangular mesh from the provided buffer. The mesh must be filled
  // with data that was encoded using the EncodeMeshToBuffer method in encode.h.
  // The function will return nullptr in case the input is invalid or if it was
  // encoded with the EncodePointCloudToBuffer method.
  StatusOr<std::unique_ptr<Mesh>> DecodeMeshFromBuffer(
      DecoderBuffer *in_buffer);

  // Decodes the buffer into a provided geometry. If the geometry is
  // incompatible with the encoded data. For example, when |out_geometry| is
  // draco::Mesh while the data contains a point cloud, the function will return
  // an error status.
  Status DecodeBufferToGeometry(DecoderBuffer *in_buffer,
                                PointCloud *out_geometry);
  Status DecodeBufferToGeometry(DecoderBuffer *in_buffer, Mesh *out_geometry);

  // When set, the decoder is going to skip attribute transform for a given
  // attribute type. For example for quantized attributes, the decoder would
  // skip the dequantization step and the returned geometry would contain an
  // attribute with quantized values. The attribute would also contain an
  // instance of AttributeTransform class that is used to describe the skipped
  // transform, including all parameters that are needed to perform the
  // transform manually.
  void SetSkipAttributeTransform(GeometryAttribute::Type att_type);

  // Returns the options instance used by the decoder that can be used by users
  // to control the decoding process.
  DecoderOptions *options() { return &options_; }

 private:
  DecoderOptions options_;
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_DECODE_H_

#include "draco/compression/config/compression_shared.h"

#ifdef DRACO_MESH_COMPRESSION_SUPPORTED
#include "draco/compression/mesh/mesh_edgebreaker_decoder.h"
#include "draco/compression/mesh/mesh_sequential_decoder.h"
#endif

#ifdef DRACO_POINT_CLOUD_COMPRESSION_SUPPORTED
#include "draco/compression/point_cloud/point_cloud_kd_tree_decoder.h"
#include "draco/compression/point_cloud/point_cloud_sequential_decoder.h"
#endif

namespace draco {

#ifdef DRACO_POINT_CLOUD_COMPRESSION_SUPPORTED
StatusOr<std::unique_ptr<PointCloudDecoder>> CreatePointCloudDecoder(
    int8_t method) {
  if (method == POINT_CLOUD_SEQUENTIAL_ENCODING) {
    return std::unique_ptr<PointCloudDecoder>(
        new PointCloudSequentialDecoder());
  } else if (method == POINT_CLOUD_KD_TREE_ENCODING) {
    return std::unique_ptr<PointCloudDecoder>(new PointCloudKdTreeDecoder());
  }
  return Status(Status::DRACO_ERROR, "Unsupported encoding method.");
}
#endif

#ifdef DRACO_MESH_COMPRESSION_SUPPORTED
StatusOr<std::unique_ptr<MeshDecoder>> CreateMeshDecoder(uint8_t method) {
  if (method == MESH_SEQUENTIAL_ENCODING) {
    return std::unique_ptr<MeshDecoder>(new MeshSequentialDecoder());
  } else if (method == MESH_EDGEBREAKER_ENCODING) {
    return std::unique_ptr<MeshDecoder>(new MeshEdgebreakerDecoder());
  }
  return Status(Status::DRACO_ERROR, "Unsupported encoding method.");
}
#endif

StatusOr<EncodedGeometryType> Decoder::GetEncodedGeometryType(
    DecoderBuffer *in_buffer) {
  DecoderBuffer temp_buffer(*in_buffer);
  DracoHeader header;
  DRACO_RETURN_IF_ERROR(PointCloudDecoder::DecodeHeader(&temp_buffer, &header));
  if (header.encoder_type >= NUM_ENCODED_GEOMETRY_TYPES) {
    return Status(Status::DRACO_ERROR, "Unsupported geometry type.");
  }
  return static_cast<EncodedGeometryType>(header.encoder_type);
}

StatusOr<std::unique_ptr<PointCloud>> Decoder::DecodePointCloudFromBuffer(
    DecoderBuffer *in_buffer) {
  DRACO_ASSIGN_OR_RETURN(EncodedGeometryType type,
                         GetEncodedGeometryType(in_buffer))
  if (type == POINT_CLOUD) {
#ifdef DRACO_POINT_CLOUD_COMPRESSION_SUPPORTED
    std::unique_ptr<PointCloud> point_cloud(new PointCloud());
    DRACO_RETURN_IF_ERROR(DecodeBufferToGeometry(in_buffer, point_cloud.get()))
    return std::move(point_cloud);
#endif
  } else if (type == TRIANGULAR_MESH) {
#ifdef DRACO_MESH_COMPRESSION_SUPPORTED
    std::unique_ptr<Mesh> mesh(new Mesh());
    DRACO_RETURN_IF_ERROR(DecodeBufferToGeometry(in_buffer, mesh.get()))
    return static_cast<std::unique_ptr<PointCloud>>(std::move(mesh));
#endif
  }
  return Status(Status::DRACO_ERROR, "Unsupported geometry type.");
}

StatusOr<std::unique_ptr<Mesh>> Decoder::DecodeMeshFromBuffer(
    DecoderBuffer *in_buffer) {
  std::unique_ptr<Mesh> mesh(new Mesh());
  DRACO_RETURN_IF_ERROR(DecodeBufferToGeometry(in_buffer, mesh.get()))
  return std::move(mesh);
}

Status Decoder::DecodeBufferToGeometry(DecoderBuffer *in_buffer,
                                       PointCloud *out_geometry) {
#ifdef DRACO_POINT_CLOUD_COMPRESSION_SUPPORTED
  DecoderBuffer temp_buffer(*in_buffer);
  DracoHeader header;
  DRACO_RETURN_IF_ERROR(PointCloudDecoder::DecodeHeader(&temp_buffer, &header))
  if (header.encoder_type != POINT_CLOUD) {
    return Status(Status::DRACO_ERROR, "Input is not a point cloud.");
  }
  DRACO_ASSIGN_OR_RETURN(std::unique_ptr<PointCloudDecoder> decoder,
                         CreatePointCloudDecoder(header.encoder_method))

  DRACO_RETURN_IF_ERROR(decoder->Decode(options_, in_buffer, out_geometry))
  return OkStatus();
#else
  return Status(Status::DRACO_ERROR, "Unsupported geometry type.");
#endif
}

Status Decoder::DecodeBufferToGeometry(DecoderBuffer *in_buffer,
                                       Mesh *out_geometry) {
#ifdef DRACO_MESH_COMPRESSION_SUPPORTED
  DecoderBuffer temp_buffer(*in_buffer);
  DracoHeader header;
  DRACO_RETURN_IF_ERROR(PointCloudDecoder::DecodeHeader(&temp_buffer, &header))
  if (header.encoder_type != TRIANGULAR_MESH) {
    return Status(Status::DRACO_ERROR, "Input is not a mesh.");
  }
  DRACO_ASSIGN_OR_RETURN(std::unique_ptr<MeshDecoder> decoder,
                         CreateMeshDecoder(header.encoder_method))

  DRACO_RETURN_IF_ERROR(decoder->Decode(options_, in_buffer, out_geometry))
  return OkStatus();
#else
  return Status(Status::DRACO_ERROR, "Unsupported geometry type.");
#endif
}

void Decoder::SetSkipAttributeTransform(GeometryAttribute::Type att_type) {
  options_.SetAttributeBool(att_type, "skip_attribute_transform", true);
}

}  // namespace draco
 */

package com.mndk.bteterrarenderer.draco.compression;

import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import com.mndk.bteterrarenderer.draco.compression.config.*;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshDecoder;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshEdgebreakerDecoder;
import com.mndk.bteterrarenderer.draco.compression.mesh.MeshSequentialDecoder;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.StatusOr;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.Getter;

/**
 * Class responsible for decoding of meshes and point clouds that were
 * compressed by a Draco encoder.
 */
@Getter
public class DracoDecoder {

    private final DecoderOptions options = new DecoderOptions();

    /**
     * Returns the geometry type encoded in the input {@code inBuffer}.
     * The return value is one of {@link EncodedGeometryType#POINT_CLOUD},
     * {@link EncodedGeometryType#TRIANGULAR_MESH} or {@link EncodedGeometryType#INVALID_GEOMETRY_TYPE}
     * in case the input data is invalid.
     * The decoded geometry type can be used to choose an appropriate decoding
     * function for a given geometry type. (see below)
     */
    public static StatusOr<EncodedGeometryType> getEncodedGeometryType(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        DecoderBuffer tempBuffer = new DecoderBuffer(inBuffer);
        DracoHeader header = new DracoHeader();
        if(PointCloudDecoder.decodeHeader(tempBuffer, header).isError(chain)) return StatusOr.error(chain.get());
        return StatusOr.ok(header.getEncoderType());
    }

//    /**
//     * Decodes point cloud from the provided buffer. The buffer must be filled
//     * with data that was encoded with either the {@link EncodePointCloudToBuffer} or
//     * {@link EncodeMeshToBuffer} methods. In case the input buffer contains
//     * mesh, the returned instance can be down-casted to {@link Mesh}.
//     */
//    public StatusOr<PointCloud> decodePointCloudFromBuffer(DecoderBuffer inBuffer) {
//
//    }

    /**
     * Decodes a triangular mesh from the provided buffer. The mesh must be filled
     * with data that was encoded using the {@link encodeMeshToBuffer} method.
     * The function will return {@code null} in case the input is invalid or if it was
     * encoded with the {@link encodePointCloudToBuffer} method.
     */
    public StatusOr<Mesh> decodeMeshFromBuffer(DecoderBuffer inBuffer) {
        StatusChain chain = new StatusChain();
        Mesh mesh = new Mesh();
        if(this.decodeBufferToGeometry(inBuffer, mesh).isError(chain)) return StatusOr.error(chain.get());
        return StatusOr.ok(mesh);
    }

//    /**
//     * Decodes the buffer into a provided geometry. If the geometry is
//     * incompatible with the encoded data. For example, when {@code outGeometry} is
//     * {@link Mesh} while the data contains a point cloud, the function will return
//     * an error status.
//     */
//    public Status decodeBufferToGeometry(DecoderBuffer inBuffer, PointCloud outGeometry) {
//
//    }
//
//    private static StatusOr<PointCloudDecoder> createPointCloudDecoder(PointCloudEncodingMethod method) {
//
//    }

    /**
     * Decodes the buffer into a provided geometry. If the geometry is
     * incompatible with the encoded data. For example, when {@code outGeometry} is
     * {@link PointCloud} while the data contains a mesh, the function will return
     * an error status.
     */
    public Status decodeBufferToGeometry(DecoderBuffer inBuffer, Mesh outGeometry) {
        StatusChain chain = new StatusChain();
        DecoderBuffer tempBuffer = new DecoderBuffer(inBuffer);
        // Parse header
        DracoHeader header = new DracoHeader();
        if(PointCloudDecoder.decodeHeader(tempBuffer, header).isError(chain)) return chain.get();
        if(header.getEncoderType() != EncodedGeometryType.TRIANGULAR_MESH) {
            return Status.dracoError("Input is not a mesh.");
        }
        // Get decoder
        StatusOr<MeshDecoder> decoderOrError = createMeshDecoder(header.getEncoderMethod());
        if(decoderOrError.isError(chain)) return chain.get();
        MeshDecoder decoder = decoderOrError.getValue();
        // Decode
        return decoder.decode(options, inBuffer, outGeometry);
    }

    private static StatusOr<MeshDecoder> createMeshDecoder(MeshEncoderMethod method) {
        if(method == MeshEncoderMethod.MESH_SEQUENTIAL_ENCODING) {
            return StatusOr.ok(new MeshSequentialDecoder());
        } else if(method == MeshEncoderMethod.MESH_EDGEBREAKER_ENCODING) {
            return StatusOr.ok(new MeshEdgebreakerDecoder());
        }
        return StatusOr.dracoError("Unsupported encoding method.");
    }

    /**
     * When set, the decoder is going to skip attribute transform for a given
     * attribute type. For example for quantized attributes, the decoder would
     * skip the dequantization step and the returned geometry would contain an
     * attribute with quantized values. The attribute would also contain an
     * instance of AttributeTransform class that is used to describe the skipped
     * transform, including all parameters that are needed to perform the
     * transform manually.
     */
    public void setSkipAttributeTransform(GeometryAttribute.Type attType) {
        options.setAttributeBool(attType, "skip_attribute_transform", true);
    }

}
