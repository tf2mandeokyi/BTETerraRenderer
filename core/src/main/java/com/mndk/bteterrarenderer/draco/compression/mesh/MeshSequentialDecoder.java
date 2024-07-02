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
#ifndef DRACO_COMPRESSION_MESH_MESH_SEQUENTIAL_DECODER_H_
#define DRACO_COMPRESSION_MESH_MESH_SEQUENTIAL_DECODER_H_

#include "draco/compression/mesh/mesh_decoder.h"

namespace draco {

// Class for decoding data encoded by MeshSequentialEncoder.
class MeshSequentialDecoder : public MeshDecoder {
 public:
  MeshSequentialDecoder();

 protected:
  bool DecodeConnectivity() override;
  bool CreateAttributesDecoder(int32_t att_decoder_id) override;

 private:
  // Decodes face indices that were compressed with an entropy code.
  // Returns false on error.
  bool DecodeAndDecompressIndices(uint32_t num_faces);
};

}  // namespace draco

#endif  // DRACO_COMPRESSION_MESH_MESH_SEQUENTIAL_DECODER_H_

#include <cstdint>
#include <limits>

#include "draco/compression/attributes/linear_sequencer.h"
#include "draco/compression/attributes/sequential_attribute_decoders_controller.h"
#include "draco/compression/entropy/symbol_decoding.h"
#include "draco/core/varint_decoding.h"

namespace draco {

MeshSequentialDecoder::MeshSequentialDecoder() {}

bool MeshSequentialDecoder::DecodeConnectivity() {
  uint32_t num_faces;
  uint32_t num_points;
#ifdef DRACO_BACKWARDS_COMPATIBILITY_SUPPORTED
  if (bitstream_version() < DRACO_BITSTREAM_VERSION(2, 2)) {
    if (!buffer()->Decode(&num_faces)) {
      return false;
    }
    if (!buffer()->Decode(&num_points)) {
      return false;
    }

  } else
#endif
  {
    if (!DecodeVarint(&num_faces, buffer())) {
      return false;
    }
    if (!DecodeVarint(&num_points, buffer())) {
      return false;
    }
  }

  // Check that num_faces and num_points are valid values.
  const uint64_t faces_64 = static_cast<uint64_t>(num_faces);
  // Compressed sequential encoding can only handle (2^32 - 1) / 3 indices.
  if (faces_64 > 0xffffffff / 3) {
    return false;
  }
  if (faces_64 > buffer()->remaining_size() / 3) {
    // The number of faces is unreasonably high, because face indices do not
    // fit in the remaining size of the buffer.
    return false;
  }
  uint8_t connectivity_method;
  if (!buffer()->Decode(&connectivity_method)) {
    return false;
  }
  if (connectivity_method == 0) {
    if (!DecodeAndDecompressIndices(num_faces)) {
      return false;
    }
  } else {
    if (num_points < 256) {
      // Decode indices as uint8_t.
      for (uint32_t i = 0; i < num_faces; ++i) {
        Mesh::Face face;
        for (int j = 0; j < 3; ++j) {
          uint8_t val;
          if (!buffer()->Decode(&val)) {
            return false;
          }
          face[j] = val;
        }
        mesh()->AddFace(face);
      }
    } else if (num_points < (1 << 16)) {
      // Decode indices as uint16_t.
      for (uint32_t i = 0; i < num_faces; ++i) {
        Mesh::Face face;
        for (int j = 0; j < 3; ++j) {
          uint16_t val;
          if (!buffer()->Decode(&val)) {
            return false;
          }
          face[j] = val;
        }
        mesh()->AddFace(face);
      }
    } else if (num_points < (1 << 21) &&
               bitstream_version() >= DRACO_BITSTREAM_VERSION(2, 2)) {
      // Decode indices as uint32_t.
      for (uint32_t i = 0; i < num_faces; ++i) {
        Mesh::Face face;
        for (int j = 0; j < 3; ++j) {
          uint32_t val;
          if (!DecodeVarint(&val, buffer())) {
            return false;
          }
          face[j] = val;
        }
        mesh()->AddFace(face);
      }
    } else {
      // Decode faces as uint32_t (default).
      for (uint32_t i = 0; i < num_faces; ++i) {
        Mesh::Face face;
        for (int j = 0; j < 3; ++j) {
          uint32_t val;
          if (!buffer()->Decode(&val)) {
            return false;
          }
          face[j] = val;
        }
        mesh()->AddFace(face);
      }
    }
  }
  point_cloud()->set_num_points(num_points);
  return true;
}

bool MeshSequentialDecoder::CreateAttributesDecoder(int32_t att_decoder_id) {
  // Always create the basic attribute decoder.
  return SetAttributesDecoder(
      att_decoder_id,
      std::unique_ptr<AttributesDecoder>(
          new SequentialAttributeDecodersController(
              std::unique_ptr<PointsSequencer>(
                  new LinearSequencer(point_cloud()->num_points())))));
}

bool MeshSequentialDecoder::DecodeAndDecompressIndices(uint32_t num_faces) {
  // Get decoded indices differences that were encoded with an entropy code.
  std::vector<uint32_t> indices_buffer(num_faces * 3);
  if (!DecodeSymbols(num_faces * 3, 1, buffer(), indices_buffer.data())) {
    return false;
  }
  // Reconstruct the indices from the differences.
  // See MeshSequentialEncoder::CompressAndEncodeIndices() for more details.
  int32_t last_index_value = 0;  // This will always be >= 0.
  int vertex_index = 0;
  for (uint32_t i = 0; i < num_faces; ++i) {
    Mesh::Face face;
    for (int j = 0; j < 3; ++j) {
      const uint32_t encoded_val = indices_buffer[vertex_index++];
      int32_t index_diff = (encoded_val >> 1);
      if (encoded_val & 1) {
        if (index_diff > last_index_value) {
          // Subtracting index_diff would result in a negative index.
          return false;
        }
        index_diff = -index_diff;
      } else {
        if (index_diff >
            (std::numeric_limits<int32_t>::max() - last_index_value)) {
          // Adding index_diff to last_index_value would overflow.
          return false;
        }
      }
      const int32_t index_value = index_diff + last_index_value;
      face[j] = index_value;
      last_index_value = index_value;
    }
    mesh()->AddFace(face);
  }
  return true;
}

}  // namespace draco
 */

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.AttributesDecoderInterface;
import com.mndk.bteterrarenderer.draco.compression.attributes.LinearSequencer;
import com.mndk.bteterrarenderer.draco.compression.attributes.PointsSequencer;
import com.mndk.bteterrarenderer.draco.compression.attributes.SequentialAttributeDecodersController;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolDecoding;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;

import java.util.concurrent.atomic.AtomicReference;

public class MeshSequentialDecoder extends MeshDecoder {
    @Override
    protected Status decodeConnectivity() {
        StatusChain chain = new StatusChain();

        AtomicReference<UInt> numFacesRef = new AtomicReference<>();
        AtomicReference<UInt> numPointsRef = new AtomicReference<>();
        if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            if(this.getBuffer().decode(DataType.uint32(), numFacesRef::set).isError(chain)) return chain.get();
            if(this.getBuffer().decode(DataType.uint32(), numPointsRef::set).isError(chain)) return chain.get();
        } else {
            if(this.getBuffer().decodeVarint(DataType.uint32(), numFacesRef).isError(chain)) return chain.get();
            if(this.getBuffer().decodeVarint(DataType.uint32(), numPointsRef).isError(chain)) return chain.get();
        }
        UInt numFaces = numFacesRef.get();
        UInt numPoints = numPointsRef.get();

        // Check that num_faces and num_points are valid values.
        // Compressed sequential encoding can only handle (2^32 - 1) / 3 indices.
        if(numFaces.gt(0xffffffffL / 3)) {
            return Status.ioError("Number of faces is too high.");
        }
        if(numFaces.gt(this.getBuffer().getRemainingSize() / 3)) {
            // The number of faces is unreasonably high, because face indices do not
            // fit in the remaining size of the buffer.
            return Status.ioError("Number of faces is too high.");
        }
        AtomicReference<UByte> connectivityMethodRef = new AtomicReference<>();
        if(this.getBuffer().decode(DataType.uint8(), connectivityMethodRef::set).isError(chain)) return chain.get();
        UByte connectivityMethod = connectivityMethodRef.get();

        if(connectivityMethod.equals(0)) {
            if(this.decodeAndDecompressIndices(numFaces).isError(chain)) return chain.get();
            if(numPoints.lt(256)) {
                // Decode indices as uint8.
                for (int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for (int j = 0; j < 3; ++j) {
                        AtomicReference<UByte> val = new AtomicReference<>();
                        if(this.getBuffer().decode(DataType.uint8(), val::set).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
            else if(numPoints.lt(1 << 16)) {
                // Decode indices as UINT16.
                for(int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for(int j = 0; j < 3; ++j) {
                        AtomicReference<UShort> val = new AtomicReference<>();
                        if(this.getBuffer().decode(DataType.uint16(), val::set).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
            else if(numPoints.lt(1 << 21) &&
                    this.getBitstreamVersion() >= DracoVersions.getBitstreamVersion(2, 2)) {
                // Decode indices as uint32.
                for(int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for(int j = 0; j < 3; ++j) {
                        AtomicReference<UInt> val = new AtomicReference<>();
                        if(this.getBuffer().decodeVarint(DataType.uint32(), val).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
            else {
                // Decode faces as uint32 (default).
                for(int i = 0; i < numFaces.intValue(); ++i) {
                    Mesh.Face face = new Mesh.Face();
                    for(int j = 0; j < 3; ++j) {
                        AtomicReference<UInt> val = new AtomicReference<>();
                        if(this.getBuffer().decode(DataType.uint32(), val::set).isError(chain)) return chain.get();
                        face.set(j, PointIndex.of(val.get().intValue()));
                    }
                    this.getMesh().addFace(face);
                }
            }
        }
        this.getPointCloud().setNumPoints(numPoints.intValue());
        return Status.ok();
    }

    @Override
    protected Status createAttributesDecoder(int attDecoderId) {
        // Always create the basic attribute decoder.
        PointsSequencer sequencer = new LinearSequencer(this.getPointCloud().getNumPoints());
        AttributesDecoderInterface decoder = new SequentialAttributeDecodersController(sequencer);
        return this.setAttributesDecoder(attDecoderId, decoder);
    }

    private Status decodeAndDecompressIndices(UInt numFaces) {
        StatusChain chain = new StatusChain();

        // Get decoded indices differences that were encoded with an entropy code.
        CppVector<UInt> indicesBuffer = CppVector.create(DataType.uint32(), numFaces.intValue() * 3);
        Status status = SymbolDecoding.decode(numFaces.mul(3), 1, this.getBuffer(), indicesBuffer);
        if(status.isError(chain)) return chain.get();

        // Reconstruct the indices from the differences.
        int lastIndexValue = 0;
        int vertexIndex = 0;
        for(UInt i = UInt.ZERO; i.lt(numFaces); i = i.add(1)) {
            Mesh.Face face = new Mesh.Face();
            for(int j = 0; j < 3; ++j) {
                UInt encodedVal = indicesBuffer.get(vertexIndex++);
                int indexDiff = encodedVal.shr(1).intValue();
                if(encodedVal.and(1).equals(1)) {
                    if(indexDiff > lastIndexValue) {
                        return Status.ioError("Index diff is too high.");
                    }
                    indexDiff = -indexDiff;
                } else {
                    if(indexDiff > Integer.MAX_VALUE - lastIndexValue) {
                        return Status.ioError("Index diff is too high.");
                    }
                }
                int indexValue = indexDiff + lastIndexValue;
                face.set(j, PointIndex.of(indexValue));
                lastIndexValue = indexValue;
            }
            this.getMesh().addFace(face);
        }
        return Status.ok();
    }
}
