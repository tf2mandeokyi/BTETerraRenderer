//// Copyright 2016 The Draco Authors.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
////      http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//#ifndef DRACO_IO_PLY_DECODER_H_
//#define DRACO_IO_PLY_DECODER_H_
//
//#include <string>
//
//#include "draco/core/decoder_buffer.h"
//#include "draco/core/status.h"
//#include "draco/draco_features.h"
//#include "draco/io/ply_reader.h"
//#include "draco/mesh/mesh.h"
//
//namespace draco {
//
//// Decodes a PLY file into draco::Mesh (or draco::PointCloud if the
//// connectivity data is not needed).
//// TODO(b/34330853): The current implementation assumes that the input vertices
//// are defined with x, y, z properties. The decoder also reads uint8 red, green,
//// blue, alpha color information, float32 defined as nx, ny, nz properties, but
//// all other attributes are ignored for now.
//class PlyDecoder {
// public:
//  PlyDecoder();
//
//  // Decodes an obj file stored in the input file.
//  Status DecodeFromFile(const std::string &file_name, Mesh *out_mesh);
//  Status DecodeFromFile(const std::string &file_name,
//                        PointCloud *out_point_cloud);
//
//  Status DecodeFromBuffer(DecoderBuffer *buffer, Mesh *out_mesh);
//  Status DecodeFromBuffer(DecoderBuffer *buffer, PointCloud *out_point_cloud);
//
// protected:
//  Status DecodeInternal();
//  DecoderBuffer *buffer() { return &buffer_; }
//
// private:
//  Status DecodeFaceData(const PlyElement *face_element);
//  Status DecodeVertexData(const PlyElement *vertex_element);
//
//  template <typename DataTypeT>
//  bool ReadPropertiesToAttribute(
//      const std::vector<const PlyProperty *> &properties,
//      PointAttribute *attribute, int num_vertices);
//
//  DecoderBuffer buffer_;
//
//  // Data structure that stores the decoded data. |out_point_cloud_| must be
//  // always set but |out_mesh_| is optional.
//  Mesh *out_mesh_;
//  PointCloud *out_point_cloud_;
//};
//
//}  // namespace draco
//
//#endif  // DRACO_IO_PLY_DECODER_H_

//// Copyright 2016 The Draco Authors.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
////      http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//#include "draco/io/ply_decoder.h"
//
//#include "draco/core/macros.h"
//#include "draco/core/status.h"
//#include "draco/io/file_utils.h"
//#include "draco/io/ply_property_reader.h"
//
//namespace draco {
//namespace {
//int64_t CountNumTriangles(const PlyElement &face_element,
//                          const PlyProperty &vertex_indices) {
//  int64_t num_triangles = 0;
//  for (int i = 0; i < face_element.num_entries(); ++i) {
//    const int64_t list_size = vertex_indices.GetListEntryNumValues(i);
//    if (list_size < 3) {
//      // Correctly encoded ply files don't have less than three vertices.
//      continue;
//    }
//    num_triangles += list_size - 2;
//  }
//  return num_triangles;
//}
//}  // namespace
//
//PlyDecoder::PlyDecoder() : out_mesh_(nullptr), out_point_cloud_(nullptr) {}
//
//Status PlyDecoder::DecodeFromFile(const std::string &file_name,
//                                  Mesh *out_mesh) {
//  out_mesh_ = out_mesh;
//  return DecodeFromFile(file_name, static_cast<PointCloud *>(out_mesh));
//}
//
//Status PlyDecoder::DecodeFromFile(const std::string &file_name,
//                                  PointCloud *out_point_cloud) {
//  std::vector<char> data;
//  if (!ReadFileToBuffer(file_name, &data)) {
//    return Status(Status::DRACO_ERROR, "Unable to read input file.");
//  }
//  buffer_.Init(data.data(), data.size());
//  return DecodeFromBuffer(&buffer_, out_point_cloud);
//}
//
//Status PlyDecoder::DecodeFromBuffer(DecoderBuffer *buffer, Mesh *out_mesh) {
//  out_mesh_ = out_mesh;
//  return DecodeFromBuffer(buffer, static_cast<PointCloud *>(out_mesh));
//}
//
//Status PlyDecoder::DecodeFromBuffer(DecoderBuffer *buffer,
//                                    PointCloud *out_point_cloud) {
//  out_point_cloud_ = out_point_cloud;
//  buffer_.Init(buffer->data_head(), buffer->remaining_size());
//  return DecodeInternal();
//}
//
//Status PlyDecoder::DecodeInternal() {
//  PlyReader ply_reader;
//  DRACO_RETURN_IF_ERROR(ply_reader.Read(buffer()));
//  // First, decode the connectivity data.
//  if (out_mesh_)
//    DRACO_RETURN_IF_ERROR(DecodeFaceData(ply_reader.GetElementByName("face")));
//  // Decode all attributes.
//  DRACO_RETURN_IF_ERROR(
//      DecodeVertexData(ply_reader.GetElementByName("vertex")));
//  // In case there are no faces this is just a point cloud which does
//  // not require deduplication.
//  if (out_mesh_ && out_mesh_->num_faces() != 0) {
//#ifdef DRACO_ATTRIBUTE_VALUES_DEDUPLICATION_SUPPORTED
//    if (!out_point_cloud_->DeduplicateAttributeValues()) {
//      return Status(Status::DRACO_ERROR,
//                    "Could not deduplicate attribute values");
//    }
//#endif
//#ifdef DRACO_ATTRIBUTE_INDICES_DEDUPLICATION_SUPPORTED
//    out_point_cloud_->DeduplicatePointIds();
//#endif
//  }
//  return OkStatus();
//}
//
//Status PlyDecoder::DecodeFaceData(const PlyElement *face_element) {
//  // We accept point clouds now.
//  if (face_element == nullptr) {
//    return OkStatus();
//  }
//  const PlyProperty *vertex_indices =
//      face_element->GetPropertyByName("vertex_indices");
//  if (vertex_indices == nullptr) {
//    // The property name may be named either "vertex_indices" or "vertex_index".
//    vertex_indices = face_element->GetPropertyByName("vertex_index");
//  }
//  if (vertex_indices == nullptr || !vertex_indices->is_list()) {
//    return Status(Status::DRACO_ERROR, "No faces defined");
//  }
//
//  // Allocate faces.
//  out_mesh_->SetNumFaces(CountNumTriangles(*face_element, *vertex_indices));
//  const int64_t num_polygons = face_element->num_entries();
//
//  PlyPropertyReader<PointIndex::ValueType> vertex_index_reader(vertex_indices);
//  Mesh::Face face;
//  FaceIndex face_index(0);
//  for (int i = 0; i < num_polygons; ++i) {
//    const int64_t list_offset = vertex_indices->GetListEntryOffset(i);
//    const int64_t list_size = vertex_indices->GetListEntryNumValues(i);
//    if (list_size < 3) {
//      continue;  // All invalid polygons are skipped.
//    }
//
//    // Triangulate polygon assuming the polygon is convex.
//    const int64_t num_triangles = list_size - 2;
//    face[0] = vertex_index_reader.ReadValue(static_cast<int>(list_offset));
//    for (int64_t ti = 0; ti < num_triangles; ++ti) {
//      for (int64_t c = 1; c < 3; ++c) {
//        face[c] = vertex_index_reader.ReadValue(
//            static_cast<int>(list_offset + ti + c));
//      }
//      out_mesh_->SetFace(face_index, face);
//      face_index++;
//    }
//  }
//  out_mesh_->SetNumFaces(face_index.value());
//  return OkStatus();
//}
//
//template <typename DataTypeT>
//bool PlyDecoder::ReadPropertiesToAttribute(
//    const std::vector<const PlyProperty *> &properties,
//    PointAttribute *attribute, int num_vertices) {
//  std::vector<std::unique_ptr<PlyPropertyReader<DataTypeT>>> readers;
//  readers.reserve(properties.size());
//  for (int prop = 0; prop < properties.size(); ++prop) {
//    readers.push_back(std::unique_ptr<PlyPropertyReader<DataTypeT>>(
//        new PlyPropertyReader<DataTypeT>(properties[prop])));
//  }
//  std::vector<DataTypeT> memory(properties.size());
//  for (PointIndex::ValueType i = 0; i < static_cast<uint32_t>(num_vertices);
//       ++i) {
//    for (int prop = 0; prop < properties.size(); ++prop) {
//      memory[prop] = readers[prop]->ReadValue(i);
//    }
//    attribute->SetAttributeValue(AttributeValueIndex(i), memory.data());
//  }
//  return true;
//}
//
//Status PlyDecoder::DecodeVertexData(const PlyElement *vertex_element) {
//  if (vertex_element == nullptr) {
//    return Status(Status::INVALID_PARAMETER, "vertex_element is null");
//  }
//  // TODO(b/34330853): For now, try to load x,y,z vertices, red,green,blue,alpha
//  // colors, and nx,ny,nz normals. We need to add other properties later.
//  const PlyProperty *const x_prop = vertex_element->GetPropertyByName("x");
//  const PlyProperty *const y_prop = vertex_element->GetPropertyByName("y");
//  const PlyProperty *const z_prop = vertex_element->GetPropertyByName("z");
//  if (!x_prop || !y_prop || !z_prop) {
//    // Currently, we require 3 vertex coordinates (this should be generalized
//    // later on).
//    return Status(Status::INVALID_PARAMETER, "x, y, or z property is missing");
//  }
//  const PointIndex::ValueType num_vertices = vertex_element->num_entries();
//  out_point_cloud_->set_num_points(num_vertices);
//  // Decode vertex positions.
//  {
//    // All properties must have the same type.
//    if (x_prop->data_type() != y_prop->data_type() ||
//        y_prop->data_type() != z_prop->data_type()) {
//      return Status(Status::INVALID_PARAMETER,
//                    "x, y, and z properties must have the same type");
//    }
//    // TODO(ostava): For now assume the position types are float32 or int32.
//    const DataType dt = x_prop->data_type();
//    if (dt != DT_FLOAT32 && dt != DT_INT32) {
//      return Status(Status::INVALID_PARAMETER,
//                    "x, y, and z properties must be of type float32 or int32");
//    }
//
//    GeometryAttribute va;
//    va.Init(GeometryAttribute::POSITION, nullptr, 3, dt, false,
//            DataTypeLength(dt) * 3, 0);
//    const int att_id = out_point_cloud_->AddAttribute(va, true, num_vertices);
//    std::vector<const PlyProperty *> properties;
//    properties.push_back(x_prop);
//    properties.push_back(y_prop);
//    properties.push_back(z_prop);
//    if (dt == DT_FLOAT32) {
//      ReadPropertiesToAttribute<float>(
//          properties, out_point_cloud_->attribute(att_id), num_vertices);
//    } else if (dt == DT_INT32) {
//      ReadPropertiesToAttribute<int32_t>(
//          properties, out_point_cloud_->attribute(att_id), num_vertices);
//    }
//  }
//
//  // Decode normals if present.
//  const PlyProperty *const n_x_prop = vertex_element->GetPropertyByName("nx");
//  const PlyProperty *const n_y_prop = vertex_element->GetPropertyByName("ny");
//  const PlyProperty *const n_z_prop = vertex_element->GetPropertyByName("nz");
//  if (n_x_prop != nullptr && n_y_prop != nullptr && n_z_prop != nullptr) {
//    // For now, all normal properties must be set and of type float32
//    if (n_x_prop->data_type() == DT_FLOAT32 &&
//        n_y_prop->data_type() == DT_FLOAT32 &&
//        n_z_prop->data_type() == DT_FLOAT32) {
//      PlyPropertyReader<float> x_reader(n_x_prop);
//      PlyPropertyReader<float> y_reader(n_y_prop);
//      PlyPropertyReader<float> z_reader(n_z_prop);
//      GeometryAttribute va;
//      va.Init(GeometryAttribute::NORMAL, nullptr, 3, DT_FLOAT32, false,
//              sizeof(float) * 3, 0);
//      const int att_id = out_point_cloud_->AddAttribute(va, true, num_vertices);
//      for (PointIndex::ValueType i = 0; i < num_vertices; ++i) {
//        std::array<float, 3> val;
//        val[0] = x_reader.ReadValue(i);
//        val[1] = y_reader.ReadValue(i);
//        val[2] = z_reader.ReadValue(i);
//        out_point_cloud_->attribute(att_id)->SetAttributeValue(
//            AttributeValueIndex(i), &val[0]);
//      }
//    }
//  }
//
//  // Decode color data if present.
//  int num_colors = 0;
//  const PlyProperty *const r_prop = vertex_element->GetPropertyByName("red");
//  const PlyProperty *const g_prop = vertex_element->GetPropertyByName("green");
//  const PlyProperty *const b_prop = vertex_element->GetPropertyByName("blue");
//  const PlyProperty *const a_prop = vertex_element->GetPropertyByName("alpha");
//  if (r_prop) {
//    ++num_colors;
//  }
//  if (g_prop) {
//    ++num_colors;
//  }
//  if (b_prop) {
//    ++num_colors;
//  }
//  if (a_prop) {
//    ++num_colors;
//  }
//
//  if (num_colors) {
//    std::vector<std::unique_ptr<PlyPropertyReader<uint8_t>>> color_readers;
//    const PlyProperty *p;
//    if (r_prop) {
//      p = r_prop;
//      // TODO(ostava): For now ensure the data type of all components is uint8.
//      DRACO_DCHECK_EQ(true, p->data_type() == DT_UINT8);
//      if (p->data_type() != DT_UINT8) {
//        return Status(Status::INVALID_PARAMETER,
//                      "Type of 'red' property must be uint8");
//      }
//      color_readers.push_back(std::unique_ptr<PlyPropertyReader<uint8_t>>(
//          new PlyPropertyReader<uint8_t>(p)));
//    }
//    if (g_prop) {
//      p = g_prop;
//      // TODO(ostava): For now ensure the data type of all components is uint8.
//      DRACO_DCHECK_EQ(true, p->data_type() == DT_UINT8);
//      if (p->data_type() != DT_UINT8) {
//        return Status(Status::INVALID_PARAMETER,
//                      "Type of 'green' property must be uint8");
//      }
//      color_readers.push_back(std::unique_ptr<PlyPropertyReader<uint8_t>>(
//          new PlyPropertyReader<uint8_t>(p)));
//    }
//    if (b_prop) {
//      p = b_prop;
//      // TODO(ostava): For now ensure the data type of all components is uint8.
//      DRACO_DCHECK_EQ(true, p->data_type() == DT_UINT8);
//      if (p->data_type() != DT_UINT8) {
//        return Status(Status::INVALID_PARAMETER,
//                      "Type of 'blue' property must be uint8");
//      }
//      color_readers.push_back(std::unique_ptr<PlyPropertyReader<uint8_t>>(
//          new PlyPropertyReader<uint8_t>(p)));
//    }
//    if (a_prop) {
//      p = a_prop;
//      // TODO(ostava): For now ensure the data type of all components is uint8.
//      DRACO_DCHECK_EQ(true, p->data_type() == DT_UINT8);
//      if (p->data_type() != DT_UINT8) {
//        return Status(Status::INVALID_PARAMETER,
//                      "Type of 'alpha' property must be uint8");
//      }
//      color_readers.push_back(std::unique_ptr<PlyPropertyReader<uint8_t>>(
//          new PlyPropertyReader<uint8_t>(p)));
//    }
//
//    GeometryAttribute va;
//    va.Init(GeometryAttribute::COLOR, nullptr, num_colors, DT_UINT8, true,
//            sizeof(uint8_t) * num_colors, 0);
//    const int32_t att_id =
//        out_point_cloud_->AddAttribute(va, true, num_vertices);
//    for (PointIndex::ValueType i = 0; i < num_vertices; ++i) {
//      std::array<uint8_t, 4> val;
//      for (int j = 0; j < num_colors; j++) {
//        val[j] = color_readers[j]->ReadValue(i);
//      }
//      out_point_cloud_->attribute(att_id)->SetAttributeValue(
//          AttributeValueIndex(i), &val[0]);
//    }
//  }
//
//  return OkStatus();
//}
//
//}  // namespace draco

package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PlyDecoder {

    @Getter(AccessLevel.PROTECTED)
    private final DecoderBuffer buffer = new DecoderBuffer();
    private Mesh outMesh = null;
    private PointCloud outPointCloud = null;

    public Status decodeFromFile(File file, Mesh outMesh) {
        this.outMesh = outMesh;
        return decodeFromFile(file, (PointCloud) outMesh);
    }

    public Status decodeFromFile(File file, PointCloud outPointCloud) {
        try(InputStream inputStream = Files.newInputStream(file.toPath())) {
            this.buffer.init(inputStream);
        } catch(Exception e) {
            return Status.ioError("Unable to read input file");
        }
        return decodeFromBuffer(this.buffer, outPointCloud);
    }

    public Status decodeFromBuffer(DecoderBuffer buffer, Mesh outMesh) {
        this.outMesh = outMesh;
        return decodeFromBuffer(buffer, (PointCloud) outMesh);
    }

    public Status decodeFromBuffer(DecoderBuffer buffer, PointCloud outPointCloud) {
        this.outPointCloud = outPointCloud;
        this.buffer.init(buffer.getDataHead(), buffer.getRemainingSize());
        return decodeInternal();
    }

    protected Status decodeInternal() {
        StatusChain chain = new StatusChain();

        PlyReader plyReader = new PlyReader();
        if(plyReader.read(buffer).isError(chain)) return chain.get();
        if(outMesh != null) {
            if(decodeFaceData(plyReader.getElementByName("face")).isError(chain)) return chain.get();
        }
        if(decodeVertexData(plyReader.getElementByName("vertex")).isError(chain)) return chain.get();

        if(outMesh != null && outMesh.getNumFaces() != 0) {
            if(outPointCloud.deduplicateAttributeValues().isError(chain)) return chain.get();
            outPointCloud.deduplicatePointIds();
        }
        return Status.ok();
    }

    private Status decodeFaceData(PlyElement faceElement) {
        if(faceElement == null) {
            return Status.ok();
        }
        PlyProperty vertexIndices = faceElement.getPropertyByName("vertex_indices");
        if(vertexIndices == null) {
            vertexIndices = faceElement.getPropertyByName("vertex_index");
        }
        if(vertexIndices == null || !vertexIndices.isList()) {
            return Status.ioError("No faces defined");
        }

        outMesh.setNumFaces(countNumTriangles(faceElement, vertexIndices));
        long numPolygons = faceElement.getNumEntries();

        PlyPropertyReader<Integer> vertexIndexReader = new PlyPropertyReader<>(DataType.int32(), vertexIndices);
        FaceIndex faceIndex = FaceIndex.of(0);
        for(int i = 0; i < numPolygons; i++) {
            Mesh.Face face = new Mesh.Face();
            long listOffset = vertexIndices.getListEntryOffset(i);
            long listSize = vertexIndices.getListEntryNumValues(i);
            if(listSize < 3) continue;

            long numTriangles = listSize - 2;
            face.set(0, PointIndex.of(vertexIndexReader.readValue((int) listOffset)));
            for(long ti = 0; ti < numTriangles; ti++) {
                for(int c = 1; c < 3; c++) {
                    face.set(c, PointIndex.of(vertexIndexReader.readValue((int) (listOffset + ti + c))));
                }
                outMesh.setFace(faceIndex, face);
                faceIndex = faceIndex.increment();
            }
        }
        outMesh.setNumFaces(faceIndex.getValue());
        return Status.ok();
    }

    private Status decodeVertexData(PlyElement vertexElement) {
        if(vertexElement == null) {
            return Status.invalidParameter("vertex_element is null");
        }

        PlyProperty xProp = vertexElement.getPropertyByName("x");
        if(xProp == null) return Status.invalidParameter("x property is missing");
        PlyProperty yProp = vertexElement.getPropertyByName("y");
        if(yProp == null) return Status.invalidParameter("y property is missing");
        PlyProperty zProp = vertexElement.getPropertyByName("z");
        if(zProp == null) return Status.invalidParameter("z property is missing");

        int numVertices = vertexElement.getNumEntries();
        outPointCloud.setNumPoints(numVertices);

        {
            if (xProp.getDataType() != yProp.getDataType() || yProp.getDataType() != zProp.getDataType()) {
                return Status.invalidParameter("x, y, and z properties must have the same type");
            }
            DracoDataType dt = xProp.getDataType();
            if (dt != DracoDataType.FLOAT32 && dt != DracoDataType.INT32) {
                return Status.invalidParameter("x, y, and z properties must be of type float32 or int32");
            }

            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.POSITION, null, UByte.of(3), dt, false, dt.getDataTypeLength() * 3, 0);
            int attId = outPointCloud.addAttribute(va, true, numVertices);
            List<PlyProperty> properties = new ArrayList<>();
            properties.add(xProp);
            properties.add(yProp);
            properties.add(zProp);
            if (dt == DracoDataType.FLOAT32) {
                readPropertiesToAttribute(DataType.float32(), properties, outPointCloud.getAttribute(attId), numVertices);
            } else {
                readPropertiesToAttribute(DataType.int32(), properties, outPointCloud.getAttribute(attId), numVertices);
            }

            PlyProperty nXProp = vertexElement.getPropertyByName("nx");
            PlyProperty nYProp = vertexElement.getPropertyByName("ny");
            PlyProperty nZProp = vertexElement.getPropertyByName("nz");
            if (nXProp != null && nYProp != null && nZProp != null) {
                DracoDataType nXType = nXProp.getDataType();
                DracoDataType nYType = nYProp.getDataType();
                DracoDataType nZType = nZProp.getDataType();
                if (nXType == DracoDataType.FLOAT32 && nYType == DracoDataType.FLOAT32 && nZType == DracoDataType.FLOAT32) {
                    PlyPropertyReader<Float> xReader = new PlyPropertyReader<>(DataType.float32(), nXProp);
                    PlyPropertyReader<Float> yReader = new PlyPropertyReader<>(DataType.float32(), nYProp);
                    PlyPropertyReader<Float> zReader = new PlyPropertyReader<>(DataType.float32(), nZProp);
                    GeometryAttribute normalAttribute = new GeometryAttribute();
                    normalAttribute.init(GeometryAttribute.Type.NORMAL, null, UByte.of(3), DracoDataType.FLOAT32, false, 4 * 3, 0);
                    int normalAttributeId = outPointCloud.addAttribute(normalAttribute, true, numVertices);
                    for (int i = 0; i < numVertices; i++) {
                        float[] val = new float[3];
                        val[0] = xReader.readValue(i);
                        val[1] = yReader.readValue(i);
                        val[2] = zReader.readValue(i);
                        Pointer<Float> pointer = Pointer.wrap(val);
                        outPointCloud.getAttribute(normalAttributeId).setAttributeValue(AttributeValueIndex.of(i), pointer);
                    }
                }
            }
        }

        int numColors = 0;
        PlyProperty rProp = vertexElement.getPropertyByName("red");
        PlyProperty gProp = vertexElement.getPropertyByName("green");
        PlyProperty bProp = vertexElement.getPropertyByName("blue");
        PlyProperty aProp = vertexElement.getPropertyByName("alpha");
        if(rProp != null) numColors++;
        if(gProp != null) numColors++;
        if(bProp != null) numColors++;
        if(aProp != null) numColors++;

        if(numColors > 0) {
            List<PlyPropertyReader<UByte>> colorReaders = new ArrayList<>();
            if(rProp != null) {
                if(rProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'red' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), rProp));
            }
            if(gProp != null) {
                if(gProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'green' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), gProp));
            }
            if(bProp != null) {
                if(bProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'blue' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), bProp));
            }
            if(aProp != null) {
                if(aProp.getDataType() != DracoDataType.UINT8) {
                    return Status.invalidParameter("Type of 'alpha' property must be uint8");
                }
                colorReaders.add(new PlyPropertyReader<>(DataType.uint8(), aProp));
            }

            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.COLOR, null, UByte.of(numColors), DracoDataType.UINT8, true, 4 * numColors, 0);
            int attId = outPointCloud.addAttribute(va, true, numVertices);
            for(int i = 0; i < numVertices; i++) {
                Pointer<UByte> val = Pointer.newUByteArray(4);
                for(int j = 0; j < numColors; j++) {
                    val.set(j, colorReaders.get(j).readValue(i));
                }
                outPointCloud.getAttribute(attId).setAttributeValue(AttributeValueIndex.of(i), val);
            }
        }

        return Status.ok();
    }

    private <T> void readPropertiesToAttribute(DataNumberType<T> type, List<PlyProperty> properties,
                                               PointAttribute attribute, int numVertices) {
        List<PlyPropertyReader<T>> readers = new ArrayList<>(properties.size());
        for(PlyProperty property : properties) {
            readers.add(new PlyPropertyReader<>(type, property));
        }
        CppVector<T> memory = new CppVector<>(type, properties.size());
        for(int i = 0; i < numVertices; i++) {
            for(int prop = 0; prop < properties.size(); prop++) {
                memory.set(prop, readers.get(prop).readValue(i));
            }
            attribute.setAttributeValue(AttributeValueIndex.of(i), memory.getPointer());
        }
    }

    private static long countNumTriangles(PlyElement faceElement, PlyProperty vertexIndices) {
        long numTriangles = 0;
        for (int i = 0; i < faceElement.getNumEntries(); ++i) {
            long listSize = vertexIndices.getListEntryNumValues(i);
            if (listSize < 3) continue;
            numTriangles += listSize - 2;
        }
        return numTriangles;
    }

}
