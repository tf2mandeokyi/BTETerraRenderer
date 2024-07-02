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
#ifndef DRACO_IO_OBJ_DECODER_H_
#define DRACO_IO_OBJ_DECODER_H_

#include <string>
#include <unordered_map>

#include "draco/core/decoder_buffer.h"
#include "draco/core/status.h"
#include "draco/draco_features.h"
#include "draco/mesh/mesh.h"

namespace draco {

// Decodes a Wavefront OBJ file into draco::Mesh (or draco::PointCloud if the
// connectivity data is not needed).. This decoder can handle decoding of
// positions, texture coordinates, normals and triangular faces.
// All other geometry properties are ignored.
class ObjDecoder {
 public:
  ObjDecoder();

  // Decodes an obj file stored in the input file.
  // Optional argument |mesh_files| will be populated with all paths to files
  // relevant to the loaded mesh.
  Status DecodeFromFile(const std::string &file_name, Mesh *out_mesh);
  Status DecodeFromFile(const std::string &file_name, Mesh *out_mesh,
                        std::vector<std::string> *mesh_files);

  Status DecodeFromFile(const std::string &file_name,
                        PointCloud *out_point_cloud);

  Status DecodeFromBuffer(DecoderBuffer *buffer, Mesh *out_mesh);
  Status DecodeFromBuffer(DecoderBuffer *buffer, PointCloud *out_point_cloud);

  // Flag that can be used to turn on/off deduplication of input values.
  // This should be disabled only when we are sure that the input data does not
  // contain any duplicate entries.
  // Default: true
  void set_deduplicate_input_values(bool v) { deduplicate_input_values_ = v; }
  // Flag for whether using metadata to record other information in the obj
  // file, e.g. material names, object names.
  void set_use_metadata(bool flag) { use_metadata_ = flag; }
  // Enables preservation of polygons.
  void set_preserve_polygons(bool flag) { preserve_polygons_ = flag; }

 protected:
  Status DecodeInternal();
  DecoderBuffer *buffer() { return &buffer_; }

 private:
  // Resets internal counters for attributes and faces.
  void ResetCounters();

  // Parses the next mesh property definition (position, tex coord, normal, or
  // face). If the parsed data is unrecognized, it will be skipped.
  // Returns false when the end of file was reached.
  bool ParseDefinition(Status *status);

  // Attempts to parse definition of position, normal, tex coord, or face
  // respectively.
  // Returns false when the parsed data didn't contain the given definition.
  bool ParseVertexPosition(Status *status);
  bool ParseNormal(Status *status);
  bool ParseTexCoord(Status *status);
  bool ParseFace(Status *status);
  bool ParseMaterialLib(Status *status);
  bool ParseMaterial(Status *status);
  bool ParseObject(Status *status);

  // Parses triplet of position, tex coords and normal indices.
  // Returns false on error.
  bool ParseVertexIndices(std::array<int32_t, 3> *out_indices);

  // Maps specified point index to the parsed vertex indices (triplet of
  // position, texture coordinate, and normal indices) .
  void MapPointToVertexIndices(PointIndex vert_id,
                               const std::array<int32_t, 3> &indices);

  // Parses material file definitions from a separate file.
  bool ParseMaterialFile(const std::string &file_name, Status *status);
  bool ParseMaterialFileDefinition(Status *status);

  // Methods related to polygon triangulation and preservation.
  static int Triangulate(int tri_index, int tri_corner);
  static bool IsNewEdge(int tri_count, int tri_index, int tri_corner);

 private:
  // If set to true, the parser will count the number of various definitions
  // but it will not parse the actual data or add any new entries to the mesh.
  bool counting_mode_;
  int num_obj_faces_;
  int num_positions_;
  int num_tex_coords_;
  int num_normals_;
  int num_materials_;
  int last_sub_obj_id_;

  int pos_att_id_;
  int tex_att_id_;
  int norm_att_id_;
  int material_att_id_;
  int sub_obj_att_id_;     // Attribute id for storing sub-objects.
  int added_edge_att_id_;  // Attribute id for polygon reconstruction.

  bool deduplicate_input_values_;

  int last_material_id_;
  std::string material_file_name_;

  std::string input_file_name_;

  std::unordered_map<std::string, int> material_name_to_id_;
  std::unordered_map<std::string, int> obj_name_to_id_;

  bool use_metadata_;

  // Polygon preservation flags.
  bool preserve_polygons_;
  bool has_polygons_;

  std::vector<std::string> *mesh_files_;

  DecoderBuffer buffer_;

  // Data structure that stores the decoded data. |out_point_cloud_| must be
  // always set but |out_mesh_| is optional.
  Mesh *out_mesh_;
  PointCloud *out_point_cloud_;
};

}  // namespace draco

#endif  // DRACO_IO_OBJ_DECODER_H_

 */

package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.metadata.AttributeMetadata;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ObjDecoder {

    private static final int FACE_MAX_CORNERS = 8;

    private boolean countingMode = true;
    private int numObjFaces = 0;
    private int numPositions = 0;
    private int numTexCoords = 0;
    private int numNormals = 0;
    private int numMaterials = 0;
    private int lastSubObjId = 0;

    private int posAttId = -1;
    private int texAttId = -1;
    private int normAttId = -1;
    private int materialAttId = -1;
    private int subObjAttId = -1;
    private int addedEdgeAttId = -1;

    private boolean deduplicateInputValues = true;

    private int lastMaterialId = 0;
    private File materialFile = null;

    private File inputFile = null;

    private final Map<String, Integer> materialNameToId = new HashMap<>();
    private final Map<String, Integer> objNameToId = new HashMap<>();

    @Setter
    private boolean useMetadata = false;

    // Polygon preservation flags.
    @Setter
    private boolean preservePolygons = false;
    private boolean hasPolygons = false;

    private List<File> meshFiles = null;

    private DecoderBuffer buffer = new DecoderBuffer();

    private Mesh outMesh = null;
    private PointCloud outPointCloud = null;

    public Status decodeFromFile(File file, Mesh outMesh) {
        return this.decodeFromFile(file, outMesh, null);
    }

    public Status decodeFromFile(File file, Mesh outMesh, List<File> meshFiles) {
        this.outMesh = outMesh;
        this.meshFiles = meshFiles;
        return decodeFromFile(file, (PointCloud) outMesh);
    }

    public Status decodeFromFile(File file, PointCloud outPointCloud) {
        try {
            InputStream stream = Files.newInputStream(file.toPath());
            DataBuffer buffer = new DataBuffer(stream);
            this.buffer.init(buffer, buffer.size());

            this.outPointCloud = outPointCloud;
            this.inputFile = file;
            return decodeInternal();
        } catch(IOException e) {
            return Status.ioError("Failed to read file: " + file.getName());
        }
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

    protected <T> Status decodeInternal() {
        StatusChain chain = new StatusChain();

        this.countingMode = true;
        this.resetCounters();
        this.materialNameToId.clear();
        this.lastSubObjId = 0;

        // Parse all lines.
        while(this.parseDefinition(chain) && chain.isOk()) {}
        if(chain.isError()) return chain.get();

        if(this.meshFiles != null && this.inputFile != null) {
            this.meshFiles.add(this.inputFile);
        }

        boolean useIdentityMapping = false;
        if(numObjFaces == 0) {
            if(numPositions == 0) {
                return Status.dracoError("No position attribute");
            }
            if(numTexCoords > 0 && numTexCoords != numPositions) {
                return Status.dracoError("Invalid number of texture coordinates for a point cloud");
            }
            if(numNormals > 0 && numNormals != numPositions) {
                return Status.dracoError("Invalid number of normals for a point cloud");
            }
            this.outMesh = null;
            useIdentityMapping = true;
        }

        // Initialize point cloud and mesh properties.
        if(this.outMesh != null) {
            this.outMesh.setNumFaces(numObjFaces);
        }
        if(numObjFaces > 0) {
            this.outPointCloud.setNumPoints(3 * numObjFaces);
        } else {
            this.outPointCloud.setNumPoints(numPositions);
        }

        // Add attributes if they are present in the input data.
        if(numPositions > 0) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.POSITION, null, UByte.of(3), DracoDataType.DT_FLOAT32, false, 3 * 4, 0);
            this.posAttId = this.outPointCloud.addAttribute(va, useIdentityMapping, numPositions);
        }
        if(numTexCoords > 0) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.TEX_COORD, null, UByte.of(2), DracoDataType.DT_FLOAT32, false, 2 * 4, 0);
            this.texAttId = this.outPointCloud.addAttribute(va, useIdentityMapping, numTexCoords);
        }
        if(numNormals > 0) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.NORMAL, null, UByte.of(3), DracoDataType.DT_FLOAT32, false, 3 * 4, 0);
            this.normAttId = this.outPointCloud.addAttribute(va, useIdentityMapping, numNormals);
        }
        if(preservePolygons && hasPolygons) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.GENERIC, null, UByte.of(1), DracoDataType.DT_UINT8, false, 1, 0);
            this.addedEdgeAttId = this.outPointCloud.addAttribute(va, false, 2);

            GeometryAttribute attribute = this.outPointCloud.getAttribute(this.addedEdgeAttId);
            for(int i = 0; i <= 1; i++) {
                AttributeValueIndex avi = AttributeValueIndex.of(i);
                attribute.setAttributeValue(avi, DataType.uint8(), UByte.of(i));
            }

            AttributeMetadata metadata = new AttributeMetadata();
            metadata.addEntryString("name", "added_edges");
            this.outPointCloud.addAttributeMetadata(this.addedEdgeAttId, metadata);
        }
        if(numMaterials > 0 && numObjFaces > 0) {
            GeometryAttribute va = new GeometryAttribute();
            GeometryAttribute.Type geometryAttributeType = GeometryAttribute.Type.GENERIC;
            DataNumberType<T, ?> dataType;
            if(numMaterials < 256) {
                va.init(geometryAttributeType, null, UByte.of(1), DracoDataType.DT_UINT8, false, 1, 0);
                dataType = BTRUtil.uncheckedCast(DataType.uint8());
            } else if(numMaterials < 65536) {
                va.init(geometryAttributeType, null, UByte.of(1), DracoDataType.DT_UINT16, false, 2, 0);
                dataType = BTRUtil.uncheckedCast(DataType.uint16());
            } else {
                va.init(geometryAttributeType, null, UByte.of(1), DracoDataType.DT_UINT32, false, 4, 0);
                dataType = BTRUtil.uncheckedCast(DataType.uint32());
            }
            this.materialAttId = this.outPointCloud.addAttribute(va, false, numMaterials);

            for(int i = 0; i < numMaterials; i++) {
                AttributeValueIndex avi = AttributeValueIndex.of(i);
                T value = dataType.from(i);
                this.outPointCloud.getAttribute(this.materialAttId).setAttributeValue(avi, dataType, value);
            }

            if(useMetadata) {
                AttributeMetadata materialMetadata = new AttributeMetadata();
                materialMetadata.addEntryString("name", "material");
                for(Map.Entry<String, Integer> entry : this.materialNameToId.entrySet()) {
                    materialMetadata.addEntryInt(entry.getKey(), entry.getValue());
                }
                if(this.materialFile != null) {
                    materialMetadata.addEntryString("file_name", this.materialFile.getName());
                }
                this.outPointCloud.addAttributeMetadata(this.materialAttId, materialMetadata);
            }
        }
        if(!this.objNameToId.isEmpty() && numObjFaces > 0) {
            GeometryAttribute va = new GeometryAttribute();
            DataNumberType<T, ?> dataType;
            if(this.objNameToId.size() < 256) {
                va.init(GeometryAttribute.Type.GENERIC, null, UByte.of(1), DracoDataType.DT_UINT8, false, 1, 0);
                dataType = BTRUtil.uncheckedCast(DataType.uint8());
            } else if(this.objNameToId.size() < 65536) {
                va.init(GeometryAttribute.Type.GENERIC, null, UByte.of(1), DracoDataType.DT_UINT16, false, 2, 0);
                dataType = BTRUtil.uncheckedCast(DataType.uint16());
            } else {
                va.init(GeometryAttribute.Type.GENERIC, null, UByte.of(1), DracoDataType.DT_UINT32, false, 4, 0);
                dataType = BTRUtil.uncheckedCast(DataType.uint32());
            }
            this.subObjAttId = this.outPointCloud.addAttribute(va, false, this.objNameToId.size());

            for(Map.Entry<String, Integer> entry : this.objNameToId.entrySet()) {
                AttributeValueIndex avi = AttributeValueIndex.of(entry.getValue());
                T value = dataType.from(entry.getValue());
                this.outPointCloud.getAttribute(this.subObjAttId).setAttributeValue(avi, dataType, value);
            }

            if(useMetadata) {
                AttributeMetadata subObjMetadata = new AttributeMetadata();
                subObjMetadata.addEntryString("name", "sub_obj");
                for(Map.Entry<String, Integer> entry : this.objNameToId.entrySet()) {
                    AttributeValueIndex avi = AttributeValueIndex.of(entry.getValue());
                    subObjMetadata.addEntryInt(entry.getKey(), entry.getValue());
                }
                this.outPointCloud.addAttributeMetadata(this.subObjAttId, subObjMetadata);
            }
        }

        // Perform a second iteration of parsing and fill all the data.
        this.countingMode = false;
        this.resetCounters();
        this.buffer.startDecodingFrom(0);
        while(this.parseDefinition(chain) && chain.get().isOk()) {}
        if(chain.get().isError()) return chain.get();
        if(this.outMesh != null) {
            Mesh.Face face = new Mesh.Face();
            for(FaceIndex i : FaceIndex.range(0, numObjFaces)) {
                for(int c = 0; c < 3; c++) {
                    face.set(c, PointIndex.of(3 * i.getValue() + c));
                }
                this.outMesh.setFace(i, face);
            }
        }

        return Status.ok();
    }

    private void resetCounters() {
        this.numObjFaces = 0;
        this.numPositions = 0;
        this.numTexCoords = 0;
        this.numNormals = 0;
        this.numMaterials = 0;
        this.lastSubObjId = 0;
    }

    private boolean parseDefinition(StatusChain chain) {
        AtomicReference<Byte> cRef = new AtomicReference<>();
        if(buffer.peek(DataType.int8(), cRef::set).isError()) {
            // End of file reached
            return false;
        }
        byte c = cRef.get();
        if(c == '#') {
            // Comment, ignore the line.
            DracoParserUtils.skipLine(buffer);
            return true;
        }
        if(this.parseVertexPosition(chain)) return true;
        if(this.parseNormal(chain)) return true;
        if(this.parseTexCoord(chain)) return true;
        if(this.parseFace(chain)) return true;
        if(this.parseMaterial(chain)) return true;
        if(this.parseMaterialLib(chain)) return true;
        if(this.parseObject(chain)) return true;
        // No known definition was found. Ignore the line.
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private Status readFloats(float[] val, int numFloats) {
        StatusChain chain = new StatusChain();
        for(int i = 0; i < numFloats; i++) {
            DracoParserUtils.skipWhitespace(buffer);
            AtomicReference<Float> valRef = new AtomicReference<>();
            if(DracoParserUtils.parseFloat(buffer, valRef).isError(chain)) return chain.get();
            val[i] = valRef.get();
        }
        return Status.ok();
    }

    private boolean parseVertexPosition(StatusChain chain) {
        byte[] c = new byte[2];
        if(buffer.peek(DataType.int8(), c, 2).isError()) return false;
        if(c[0] != 'v' || c[1] != ' ') return false;

        // Vertex definition found!
        buffer.advance(2);
        if(!countingMode) {
            // Parse three float numbers for vertex position coordinates.
            float[] val = new float[3];
            if(this.readFloats(val, 3).isError(chain)) return true;

            GeometryAttribute attribute = this.outPointCloud.getAttribute(this.posAttId);
            attribute.setAttributeValues(AttributeValueIndex.of(numPositions), DataType.float32(), val, 3);
        }
        numPositions++;
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseNormal(StatusChain chain) {
        byte[] c = new byte[2];
        if(buffer.peek(DataType.int8(), c, 2).isError()) return false;
        if(c[0] != 'v' || c[1] != 'n') return false;

        // Normal definition found!
        buffer.advance(2);
        if(!countingMode) {
            // Parse three float numbers for the normal vector.
            float[] val = new float[3];
            if(this.readFloats(val, 3).isError(chain)) return true;

            GeometryAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
            attribute.setAttributeValues(AttributeValueIndex.of(numNormals), DataType.float32(), val, 3);
        }
        numNormals++;
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseTexCoord(StatusChain chain) {
        byte[] c = new byte[2];
        if(buffer.peek(DataType.int8(), c, 2).isError()) return false;
        if(c[0] != 'v' || c[1] != 't') return false;

        // Texture coord definition found!
        buffer.advance(2);
        if(!countingMode) {
            // Parse two float numbers for the texture coordinate.
            float[] val = new float[2];
            if(this.readFloats(val, 2).isError(chain)) return true;

            GeometryAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
            attribute.setAttributeValues(AttributeValueIndex.of(numTexCoords), DataType.float32(), val, 2);
        }
        numTexCoords++;
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseFace(StatusChain chain) {
        AtomicReference<Byte> cRef = new AtomicReference<>();
        if(buffer.peek(DataType.int8(), cRef::set).isError()) return false;
        if(cRef.get() != 'f') return false;

        // Face definition found!
        buffer.advance(1);
        if(!countingMode) {
            // Parse face indices.
            int numValidIndices = 0;
            int[][] indices = new int[FACE_MAX_CORNERS][3];
            for(int i = 0; i < FACE_MAX_CORNERS; i++) {
                if(this.parseVertexIndices(indices[i]).isError(chain)) {
                    if(i >= 3) {
                        chain.set(Status.ok());
                        break; // It's OK if there is no fourth or higher vertex index.
                    }
                    return true;
                }
                numValidIndices++;
            }
            // Split quads and other n-gons into n - 2 triangles.
            int nt = numValidIndices - 2;
            // Iterate over triangles.
            for(int t = 0; t < nt; t++) {
                // Iterate over corners.
                for(int c = 0; c < 3; c++) {
                    PointIndex vertId = PointIndex.of(3 * numObjFaces + c);
                    int triangulatedIndex = triangulate(t, c);
                    this.mapPointToVertexIndices(vertId, indices[triangulatedIndex]);
                    if(this.addedEdgeAttId >= 0) {
                        AttributeValueIndex avi = AttributeValueIndex.of(isNewEdge(nt, t, c) ? 1 : 0);
                        this.outPointCloud.getAttribute(this.addedEdgeAttId).setPointMapEntry(vertId, avi);
                    }
                }
                numObjFaces++;
            }
        } else {
            // We are in the counting mode.
            DracoParserUtils.skipWhitespace(buffer);
            int numIndices = 0;
            AtomicBoolean isEnd = new AtomicBoolean();
            while (buffer.peek(DataType.int8(), cRef::set).isOk() && cRef.get() != '\n') {
                if (DracoParserUtils.peekWhitespace(buffer, isEnd) && isEnd.get()) {
                    buffer.advance(1);
                } else {
                    numIndices++;
                    while (!DracoParserUtils.peekWhitespace(buffer, isEnd) && !isEnd.get()) {
                        buffer.advance(1);
                    }
                }
            }
            if (numIndices > 3) {
                hasPolygons = true;
            }
            if (numIndices < 3 || numIndices > FACE_MAX_CORNERS) {
                chain.set(Status.dracoError("Invalid number of indices on a face"));
                return false;
            }
            numObjFaces += numIndices - 2;
        }
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseMaterialLib(StatusChain chain) {
        if(!this.materialNameToId.isEmpty()) return false;

        AtomicReference<String> mtllibRef = new AtomicReference<>();
        if(buffer.peek(DataType.string(6), mtllibRef::set).isError()) return false;
        if(!"mtllib".equals(mtllibRef.get())) return false;

        buffer.advance(6);
        DecoderBuffer lineBuffer = DracoParserUtils.parseLineIntoDecoderBuffer(buffer);
        DracoParserUtils.skipWhitespace(lineBuffer);
        AtomicReference<String> materialFileName = new AtomicReference<>();
        if(DracoParserUtils.parseString(lineBuffer, materialFileName).isError(chain)) return true;
        DracoParserUtils.skipLine(lineBuffer);

        if(!materialFileName.get().isEmpty()) {
            File directory = inputFile.getParentFile();
            if(directory != null) {
                this.materialFile = new File(directory, materialFileName.get());
            } else {
                this.materialFile = new File(materialFileName.get());
            }
            if(this.meshFiles != null) {
                this.meshFiles.add(this.materialFile);
            }
            // Silently ignore problems with material files for now.
            if(this.parseMaterialFile(materialFile, chain).isError(chain)) return true;
        }
        return true;
    }

    private boolean parseMaterial(StatusChain chain) {
        if(!countingMode && materialAttId < 0) return false;

        AtomicReference<String> usemtlRef = new AtomicReference<>();
        if(buffer.peek(DataType.string(6), usemtlRef::set).isError()) return false;
        if(!"usemtl".equals(usemtlRef.get())) return false;

        buffer.advance(6);
        DecoderBuffer lineBuffer = DracoParserUtils.parseLineIntoDecoderBuffer(buffer);
        DracoParserUtils.skipWhitespace(lineBuffer);
        AtomicReference<String> matName = new AtomicReference<>();
        DracoParserUtils.parseLine(lineBuffer, matName);
        if(matName.get().isEmpty()) return false;

        if(!this.materialNameToId.containsKey(matName.get())) {
            this.lastMaterialId = this.numMaterials;
            this.materialNameToId.put(matName.get(), this.numMaterials++);
            return true;
        }
        this.lastMaterialId = this.materialNameToId.get(matName.get());
        return true;
    }

    private boolean parseObject(StatusChain chain) {
        AtomicReference<String> oRef = new AtomicReference<>();
        if(buffer.peek(DataType.string(2), oRef::set).isError()) return false;
        if(!"o ".equals(oRef.get())) return false;

        buffer.advance(1);
        DecoderBuffer lineBuffer = DracoParserUtils.parseLineIntoDecoderBuffer(buffer);
        DracoParserUtils.skipWhitespace(lineBuffer);
        AtomicReference<String> objName = new AtomicReference<>();
        if(DracoParserUtils.parseString(lineBuffer, objName).isError(chain)) return true;
        if(objName.get().isEmpty()) return true;

        if(!this.objNameToId.containsKey(objName.get())) {
            this.objNameToId.put(objName.get(), this.objNameToId.size());
            this.lastSubObjId = this.objNameToId.size() - 1;
        } else {
            this.lastSubObjId = this.objNameToId.get(objName.get());
        }
        return true;
    }

    private Status parseMaterialFile(File materialFile, StatusChain chain) {
        try {
            // Back up the original decoder buffer.
            DecoderBuffer oldBuffer = this.buffer;

            InputStream is = Files.newInputStream(materialFile.toPath());
            this.buffer.init(is);

            this.numMaterials = 0;
            while(this.parseMaterialFileDefinition(chain)) {}

            // Restore the original buffer.
            buffer = oldBuffer;
            return Status.ok();
        } catch(IOException e) {
            return Status.ioError("Failed to read material file: " + materialFile.getName());
        }
    }

    private boolean parseMaterialFileDefinition(StatusChain chain) {
        AtomicReference<Byte> cRef = new AtomicReference<>();
        if(buffer.peek(DataType.int8(), cRef::set).isError()) return false;
        if(cRef.get() == '#') {
            // Comment, ignore the line.
            DracoParserUtils.skipLine(buffer);
            return true;
        }
        AtomicReference<String> strRef = new AtomicReference<>();
        if(DracoParserUtils.parseString(buffer, strRef).isError()) return false;
        if("newmtl".equals(strRef.get())) {
            DracoParserUtils.skipWhitespace(buffer);
            DracoParserUtils.parseLine(buffer, strRef);
            if(strRef.get().isEmpty()) {
                return false;
            }
            // Add new material to our map.
            this.materialNameToId.put(strRef.get(), this.numMaterials++);
        }
        return true;
    }

    private Status parseVertexIndices(int[] outIndices) {
        StatusChain chain = new StatusChain();

        // Parsed attribute indices can be in format:
        // 1. POS_INDEX
        // 2. POS_INDEX/TEX_COORD_INDEX
        // 3. POS_INDEX/TEX_COORD_INDEX/NORMAL_INDEX
        // 4. POS_INDEX//NORMAL_INDEX
        DracoParserUtils.skipCharacters(buffer, " \t");
        AtomicReference<Integer> indexRef = new AtomicReference<>();
        if (!DracoParserUtils.parseSignedInt(buffer, indexRef).isError(chain)) return chain.get();
        if (indexRef.get() == 0) return Status.dracoError("Position index must be valid");
        outIndices[0] = indexRef.get();
        outIndices[1] = 0;
        outIndices[2] = 0;
        AtomicReference<Byte> chRef = new AtomicReference<>();
        // It may be OK if we cannot read any more characters.
        if(buffer.peek(DataType.int8(), chRef::set).isError()) return Status.ok();
        byte ch = chRef.get();
        if(ch != '/') {
            return Status.ok();
        }
        buffer.advance(1);
        if(buffer.peek(DataType.int8(), chRef::set).isError(chain)) return chain.get();
        ch = chRef.get();
        if(ch != '/') {
            // Must be texture coord index.
            if (!DracoParserUtils.parseSignedInt(buffer, indexRef).isError(chain)) return chain.get();
            if (indexRef.get() == 0) return Status.dracoError("Texture index must be valid");
            outIndices[1] = indexRef.get();
        }
        if(buffer.peek(DataType.int8(), chRef::set).isError(chain)) return chain.get();
        ch = chRef.get();
        if(ch == '/') {
            buffer.advance(1);
            // Read normal index.
            if (!DracoParserUtils.parseSignedInt(buffer, indexRef).isError(chain)) return chain.get();
            if (indexRef.get() == 0) return Status.dracoError("Normal index must be valid");
            outIndices[2] = indexRef.get();
        }
        return Status.ok();
    }

    private void mapPointToVertexIndices(PointIndex vertId, int[] indices) {
        if(indices[0] > 0) {
            PointAttribute attribute = this.outPointCloud.getAttribute(this.posAttId);
            attribute.setPointMapEntry(vertId, AttributeValueIndex.of(indices[0] - 1));
        } else if(indices[0] < 0) {
            PointAttribute attribute = this.outPointCloud.getAttribute(this.posAttId);
            attribute.setPointMapEntry(vertId, AttributeValueIndex.of(numPositions + indices[0]));
        }

        if(texAttId >= 0) {
            if(indices[1] > 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(indices[1] - 1));
            } else if(indices[1] < 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(numTexCoords + indices[1]));
            } else {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(0));
            }
        }

        if(normAttId >= 0) {
            if(indices[2] > 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(indices[2] - 1));
            } else if(indices[2] < 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(numNormals + indices[2]));
            } else {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(0));
            }
        }

        if(materialAttId >= 0) {
            PointAttribute attribute = this.outPointCloud.getAttribute(this.materialAttId);
            attribute.setPointMapEntry(vertId, AttributeValueIndex.of(lastMaterialId));
        }

        if(subObjAttId >= 0) {
            PointAttribute attribute = this.outPointCloud.getAttribute(this.subObjAttId);
            attribute.setPointMapEntry(vertId, AttributeValueIndex.of(lastSubObjId));
        }
    }

    private int triangulate(int triIndex, int triCorner) {
        return triCorner == 0 ? 0 : triIndex + triCorner;
    }

    private boolean isNewEdge(int triCount, int triIndex, int triCorner) {
        return triIndex != triCount - 1 && triCorner == 1;
    }
}
