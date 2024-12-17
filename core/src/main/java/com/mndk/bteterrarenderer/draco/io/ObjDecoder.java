/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.io;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
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

@SuppressWarnings("StatementWithEmptyBody")
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

    @Setter
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

    public Status decodeFromFile(File file, Mesh outMesh, List<File> meshFiles) {
        this.meshFiles = meshFiles;
        return decodeFromFile(file, outMesh);
    }

    public Status decodeFromFile(File file, PointCloud outPointCloud) {
        try {
            InputStream stream = Files.newInputStream(file.toPath());
            this.buffer.init(stream);

            this.outPointCloud = outPointCloud;
            this.inputFile = file;
            if (outPointCloud instanceof Mesh) {
                this.outMesh = (Mesh) outPointCloud;
            }
            return decodeInternal();
        } catch (IOException e) {
            return Status.ioError("Failed to read file: " + file.getName(), e);
        }
    }

    public Status decodeFromBuffer(DecoderBuffer buffer, PointCloud outPointCloud) {
        this.outPointCloud = outPointCloud;
        if (outPointCloud instanceof Mesh) {
            this.outMesh = (Mesh) outPointCloud;
        }
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
        while (this.parseDefinition(chain) && chain.isOk()) {}
        if (chain.isError()) return chain.get();

        if (this.meshFiles != null && this.inputFile != null) {
            this.meshFiles.add(this.inputFile);
        }

        boolean useIdentityMapping = false;

        if (numObjFaces == 0) {
            if (numPositions == 0) {
                return Status.dracoError("No position attribute");
            }
            if (numTexCoords > 0 && numTexCoords != numPositions) {
                return Status.dracoError("Invalid number of texture coordinates for a point cloud");
            }
            if (numNormals > 0 && numNormals != numPositions) {
                return Status.dracoError("Invalid number of normals for a point cloud");
            }
            this.outMesh = null;
            useIdentityMapping = true;
        }

        // Initialize point cloud and mesh properties.
        if (this.outMesh != null) {
            this.outMesh.setNumFaces(numObjFaces);
        }
        if (numObjFaces > 0) {
            this.outPointCloud.setNumPoints(3 * numObjFaces);
        } else {
            this.outPointCloud.setNumPoints(numPositions);
        }

        // Add attributes if they are present in the input data.
        if (numPositions > 0) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.POSITION, null, 3, DracoDataType.FLOAT32, false);
            this.posAttId = this.outPointCloud.addAttribute(va, useIdentityMapping, numPositions);
        }
        if (numTexCoords > 0) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.TEX_COORD, null, 2, DracoDataType.FLOAT32, false);
            this.texAttId = this.outPointCloud.addAttribute(va, useIdentityMapping, numTexCoords);
        }
        if (numNormals > 0) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.NORMAL, null, 3, DracoDataType.FLOAT32, false);
            this.normAttId = this.outPointCloud.addAttribute(va, useIdentityMapping, numNormals);
        }
        if (preservePolygons && hasPolygons) {
            GeometryAttribute va = new GeometryAttribute();
            va.init(GeometryAttribute.Type.GENERIC, null, 1, DracoDataType.UINT8, false);
            PointCloud pc = this.outPointCloud;
            this.addedEdgeAttId = pc.addAttribute(va, false, 2);

            GeometryAttribute attribute = pc.getAttribute(this.addedEdgeAttId);
            for (int i = 0; i <= 1; i++) {
                AttributeValueIndex avi = AttributeValueIndex.of(i);
                attribute.setAttributeValue(avi, Pointer.newUByte((byte) i));
            }

            AttributeMetadata metadata = new AttributeMetadata();
            metadata.addEntryString("name", "added_edges");
            pc.addAttributeMetadata(this.addedEdgeAttId, metadata);
        }
        if (numMaterials > 0 && numObjFaces > 0) {
            GeometryAttribute va = new GeometryAttribute();
            GeometryAttribute.Type geometryAttributeType = GeometryAttribute.Type.GENERIC;
            DataNumberType<T> dataType;
            if (numMaterials < 256) {
                va.init(geometryAttributeType, null, 1, DracoDataType.UINT8, false);
                dataType = BTRUtil.uncheckedCast(DataType.uint8());
            } else if (numMaterials < 65536) {
                va.init(geometryAttributeType, null, 1, DracoDataType.UINT16, false);
                dataType = BTRUtil.uncheckedCast(DataType.uint16());
            } else {
                va.init(geometryAttributeType, null, 1, DracoDataType.UINT32, false);
                dataType = BTRUtil.uncheckedCast(DataType.uint32());
            }
            this.materialAttId = this.outPointCloud.addAttribute(va, false, numMaterials);

            for (int i = 0; i < numMaterials; i++) {
                AttributeValueIndex avi = AttributeValueIndex.of(i);
                Pointer<T> value = dataType.newOwned(dataType.from(i));
                this.outPointCloud.getAttribute(this.materialAttId).setAttributeValue(avi, value);
            }

            if (useMetadata) {
                AttributeMetadata materialMetadata = new AttributeMetadata();
                materialMetadata.addEntryString("name", "material");
                for (Map.Entry<String, Integer> entry : this.materialNameToId.entrySet()) {
                    materialMetadata.addEntryInt(entry.getKey(), entry.getValue());
                }
                if (this.materialFile != null) {
                    materialMetadata.addEntryString("file_name", this.materialFile.getName());
                }
                this.outPointCloud.addAttributeMetadata(this.materialAttId, materialMetadata);
            }
        }
        if (!this.objNameToId.isEmpty() && numObjFaces > 0) {
            GeometryAttribute va = new GeometryAttribute();
            DataNumberType<T> dataType;
            if (this.objNameToId.size() < 256) {
                va.init(GeometryAttribute.Type.GENERIC, null, 1, DracoDataType.UINT8, false);
                dataType = BTRUtil.uncheckedCast(DataType.uint8());
            } else if (this.objNameToId.size() < 65536) {
                va.init(GeometryAttribute.Type.GENERIC, null, 1, DracoDataType.UINT16, false);
                dataType = BTRUtil.uncheckedCast(DataType.uint16());
            } else {
                va.init(GeometryAttribute.Type.GENERIC, null, 1, DracoDataType.UINT32, false);
                dataType = BTRUtil.uncheckedCast(DataType.uint32());
            }
            this.subObjAttId = this.outPointCloud.addAttribute(va, false, this.objNameToId.size());

            for (Map.Entry<String, Integer> entry : this.objNameToId.entrySet()) {
                AttributeValueIndex avi = AttributeValueIndex.of(entry.getValue());
                Pointer<T> value = dataType.newOwned(dataType.from(entry.getValue()));
                this.outPointCloud.getAttribute(this.subObjAttId).setAttributeValue(avi, value);
            }

            if (useMetadata) {
                AttributeMetadata subObjMetadata = new AttributeMetadata();
                subObjMetadata.addEntryString("name", "sub_obj");
                for (Map.Entry<String, Integer> entry : this.objNameToId.entrySet()) {
                    subObjMetadata.addEntryInt(entry.getKey(), entry.getValue());
                }
                this.outPointCloud.addAttributeMetadata(this.subObjAttId, subObjMetadata);
            }
        }

        // Perform a second iteration of parsing and fill all the data.
        this.countingMode = false;
        this.resetCounters();
        this.buffer.startDecodingFrom(0);
        while (this.parseDefinition(chain) && chain.get().isOk()) {}
        if (chain.get().isError()) return chain.get();
        if (this.outMesh != null) {
            for (FaceIndex i : FaceIndex.range(0, numObjFaces)) {
                Mesh.Face face = new Mesh.Face();
                for (int c = 0; c < 3; c++) {
                    face.set(c, PointIndex.of(3 * i.getValue() + c));
                }
                this.outMesh.setFace(i, face);
            }
        }

        if (deduplicateInputValues) {
            if (outPointCloud.deduplicateAttributeValues().isError(chain)) return chain.get();
        }
        outPointCloud.deduplicatePointIds();

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
        Pointer<Byte> cRef = Pointer.newByte();
        if (buffer.peek(cRef).isError()) {
            // End of file reached
            return false;
        }
        byte c = cRef.get();
        if (c == '#') {
            // Comment, ignore the line.
            DracoParserUtils.skipLine(buffer);
            return true;
        }
        if (this.parseVertexPosition(chain)) return true;
        if (this.parseNormal(chain)) return true;
        if (this.parseTexCoord(chain)) return true;
        if (this.parseFace(chain)) return true;
        if (this.parseMaterial()) return true;
        if (this.parseMaterialLib(chain)) return true;
        if (this.parseObject(chain)) return true;
        // No known definition was found. Ignore the line.
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private Status readFloats(float[] val, int numFloats) {
        StatusChain chain = new StatusChain();
        for (int i = 0; i < numFloats; i++) {
            DracoParserUtils.skipWhitespace(buffer);
            AtomicReference<Float> valRef = new AtomicReference<>();
            if (DracoParserUtils.parseFloat(buffer, valRef).isError(chain)) return chain.get();
            val[i] = valRef.get();
        }
        return Status.ok();
    }

    private boolean parseVertexPosition(StatusChain chain) {
        RawPointer cBuffer = RawPointer.newArray(2);
        if (buffer.peek(cBuffer, 2).isError()) return false;
        if (!PointerHelper.rawToString(cBuffer, 2).equals("v ")) return false;

        // Vertex definition found!
        buffer.advance(2);
        if (!countingMode) {
            // Parse three float numbers for vertex position coordinates.
            float[] val = new float[3];
            if (this.readFloats(val, 3).isError(chain)) return true;

            GeometryAttribute attribute = this.outPointCloud.getAttribute(this.posAttId);
            attribute.setAttributeValue(AttributeValueIndex.of(numPositions), Pointer.wrap(val));
        }
        numPositions++;
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseNormal(StatusChain chain) {
        RawPointer cBuffer = RawPointer.newArray(2);
        if (buffer.peek(cBuffer, 2).isError()) return false;
        if (!PointerHelper.rawToString(cBuffer, 2).equals("vn")) return false;

        // Normal definition found!
        buffer.advance(2);
        if (!countingMode) {
            // Parse three float numbers for the normal vector.
            float[] val = new float[3];
            if (this.readFloats(val, 3).isError(chain)) return true;

            GeometryAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
            attribute.setAttributeValue(AttributeValueIndex.of(numNormals), Pointer.wrap(val));
        }
        numNormals++;
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseTexCoord(StatusChain chain) {
        RawPointer cBuffer = RawPointer.newArray(2);
        if (buffer.peek(cBuffer, 2).isError()) return false;
        if (!PointerHelper.rawToString(cBuffer, 2).equals("vt")) return false;

        // Texture coord definition found!
        buffer.advance(2);
        if (!countingMode) {
            // Parse two float numbers for the texture coordinate.
            float[] val = new float[2];
            if (this.readFloats(val, 2).isError(chain)) return true;

            GeometryAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
            attribute.setAttributeValue(AttributeValueIndex.of(numTexCoords), Pointer.wrap(val));
        }
        numTexCoords++;
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseFace(StatusChain chain) {
        Pointer<Byte> cRef = Pointer.newByte();
        if (buffer.peek(cRef).isError()) return false;
        if (cRef.get() != 'f') return false;

        // Face definition found!
        buffer.advance(1);
        if (!countingMode) {
            // Parse face indices.
            int numValidIndices = 0;
            int[][] indices = new int[FACE_MAX_CORNERS][3];
            for (int i = 0; i < FACE_MAX_CORNERS; i++) {
                if (this.parseVertexIndices(indices[i]).isError(chain)) {
                    if (i >= 3) {
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
            for (int t = 0; t < nt; t++) {
                // Iterate over corners.
                for (int c = 0; c < 3; c++) {
                    PointIndex vertId = PointIndex.of(3 * numObjFaces + c);
                    int triangulatedIndex = triangulate(t, c);
                    this.mapPointToVertexIndices(vertId, indices[triangulatedIndex]);
                    if (this.addedEdgeAttId >= 0) {
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
            while (buffer.peek(cRef).isOk() && cRef.get() != '\n') {
                if (DracoParserUtils.peekWhitespace(buffer, isEnd)) {
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
                chain.set(Status.dracoError("Invalid number of indices on a face: " + numIndices));
                return false;
            }
            numObjFaces += numIndices - 2;
        }
        DracoParserUtils.skipLine(buffer);
        return true;
    }

    private boolean parseMaterialLib(StatusChain chain) {
        if (!this.materialNameToId.isEmpty()) return false;

        RawPointer cBuffer = RawPointer.newArray(6);
        if (buffer.peek(cBuffer, 6).isError()) return false;
        if (!PointerHelper.rawToString(cBuffer, 6).equals("mtllib")) return false;

        buffer.advance(6);
        DecoderBuffer lineBuffer = DracoParserUtils.parseLineIntoDecoderBuffer(buffer);
        DracoParserUtils.skipWhitespace(lineBuffer);
        AtomicReference<String> materialFileName = new AtomicReference<>();
        if (DracoParserUtils.parseString(lineBuffer, materialFileName).isError(chain)) return true;
        DracoParserUtils.skipLine(lineBuffer);

        if (!materialFileName.get().isEmpty()) {
            File directory = inputFile.getParentFile();
            if (directory != null) {
                this.materialFile = new File(directory, materialFileName.get());
            } else {
                this.materialFile = new File(materialFileName.get());
            }
            if (this.meshFiles != null) {
                this.meshFiles.add(this.materialFile);
            }
            // Silently ignore problems with material files for now.
            if (this.parseMaterialFile(materialFile).isError(chain)) return true;
        }
        return true;
    }

    private boolean parseMaterial() {
        if (!countingMode && materialAttId < 0) return false;

        RawPointer cBuffer = RawPointer.newArray(6);
        if (buffer.peek(cBuffer, 6).isError()) return false;
        if (!PointerHelper.rawToString(cBuffer, 6).equals("usemtl")) return false;

        buffer.advance(6);
        DecoderBuffer lineBuffer = DracoParserUtils.parseLineIntoDecoderBuffer(buffer);
        DracoParserUtils.skipWhitespace(lineBuffer);
        AtomicReference<String> matName = new AtomicReference<>();
        DracoParserUtils.parseLine(lineBuffer, matName);
        if (matName.get().isEmpty()) return false;

        if (!this.materialNameToId.containsKey(matName.get())) {
            this.lastMaterialId = this.numMaterials;
            this.materialNameToId.put(matName.get(), this.numMaterials++);
            return true;
        }
        this.lastMaterialId = this.materialNameToId.get(matName.get());
        return true;
    }

    private boolean parseObject(StatusChain chain) {
        RawPointer cBuffer = RawPointer.newArray(2);
        if (buffer.peek(cBuffer, 2).isError()) return false;
        if (!PointerHelper.rawToString(cBuffer, 2).equals("o ")) return false;

        buffer.advance(1);
        DecoderBuffer lineBuffer = DracoParserUtils.parseLineIntoDecoderBuffer(buffer);
        DracoParserUtils.skipWhitespace(lineBuffer);
        AtomicReference<String> objNameRef = new AtomicReference<>();
        if (DracoParserUtils.parseString(lineBuffer, objNameRef).isError(chain)) return true;
        String objName = objNameRef.get();
        if (objName.isEmpty()) return true;

        if (!this.objNameToId.containsKey(objName)) {
            this.objNameToId.put(objName, this.objNameToId.size());
            this.lastSubObjId = this.objNameToId.size() - 1;
        } else {
            this.lastSubObjId = this.objNameToId.get(objName);
        }
        return true;
    }

    private Status parseMaterialFile(File materialFile) {
        // Back up the original decoder buffer.
        DecoderBuffer oldBuffer = new DecoderBuffer(this.buffer);

        try (InputStream is = Files.newInputStream(materialFile.toPath())) {
            this.buffer.init(is);
            this.numMaterials = 0;
            while (this.parseMaterialFileDefinition()) {}
            return Status.ok();
        } catch (IOException ignored) {
            // Silently ignore problems with material files for now.
            return Status.ok();
        } finally {
            // Restore the original buffer.
            buffer = oldBuffer;
        }
    }

    private boolean parseMaterialFileDefinition() {
        Pointer<Byte> cRef = Pointer.newByte();
        if (buffer.peek(cRef).isError()) return false;
        if (cRef.get() == '#') {
            // Comment, ignore the line.
            DracoParserUtils.skipLine(buffer);
            return true;
        }
        AtomicReference<String> strRef = new AtomicReference<>();
        if (DracoParserUtils.parseString(buffer, strRef).isError()) return false;
        if ("newmtl".equals(strRef.get())) {
            DracoParserUtils.skipWhitespace(buffer);
            DracoParserUtils.parseLine(buffer, strRef);
            String str = strRef.get();
            if (str.isEmpty()) {
                return false;
            }
            // Add new material to our map.
            this.materialNameToId.put(str, this.numMaterials++);
        }
        return true;
    }

    private Status parseVertexIndices(int[] outIndices) {
        StatusChain chain = new StatusChain();
        AtomicReference<Integer> indexRef = new AtomicReference<>();
        Pointer<Byte> chRef = Pointer.newByte();

        // Parsed attribute indices can be in format:
        // 1. POS_INDEX
        // 2. POS_INDEX/TEX_COORD_INDEX
        // 3. POS_INDEX/TEX_COORD_INDEX/NORMAL_INDEX
        // 4. POS_INDEX//NORMAL_INDEX
        DracoParserUtils.skipCharacters(buffer, new byte[] { ' ', '\t' });

        // Read position index.
        if (DracoParserUtils.parseSignedInt(buffer, indexRef).isError(chain)) return chain.get();
        if (indexRef.get() == 0) return Status.dracoError("Position index must be valid");
        outIndices[0] = indexRef.get();
        outIndices[1] = 0;
        outIndices[2] = 0;

        // It may be OK if we cannot read any more characters (see the first format)
        if (buffer.peek(chRef).isError()) return Status.ok();
        byte ch = chRef.get();
        if (ch != '/') return Status.ok();
        buffer.advance(1);

        if (buffer.peek(chRef).isError(chain)) return chain.get();
        ch = chRef.get();
        if (ch != '/') {
            // Must be texture coord index.
            if (DracoParserUtils.parseSignedInt(buffer, indexRef).isError(chain)) return chain.get();
            if (indexRef.get() == 0) return Status.dracoError("Texture index must be valid");
            outIndices[1] = indexRef.get();
        }

        if (buffer.peek(chRef).isError(chain)) return chain.get();
        ch = chRef.get();
        if (ch == '/') {
            buffer.advance(1);
            // Read normal index.
            if (DracoParserUtils.parseSignedInt(buffer, indexRef).isError(chain)) return chain.get();
            if (indexRef.get() == 0) return Status.dracoError("Normal index must be valid");
            outIndices[2] = indexRef.get();
        }
        return Status.ok();
    }

    private void mapPointToVertexIndices(PointIndex vertId, int[] indices) {
        if (indices[0] > 0) {
            PointAttribute attribute = this.outPointCloud.getAttribute(this.posAttId);
            attribute.setPointMapEntry(vertId, AttributeValueIndex.of(indices[0] - 1));
        } else if (indices[0] < 0) {
            PointAttribute attribute = this.outPointCloud.getAttribute(this.posAttId);
            attribute.setPointMapEntry(vertId, AttributeValueIndex.of(numPositions + indices[0]));
        }

        if (texAttId >= 0) {
            if (indices[1] > 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(indices[1] - 1));
            } else if (indices[1] < 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(numTexCoords + indices[1]));
            } else {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.texAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(0));
            }
        }

        if (normAttId >= 0) {
            if (indices[2] > 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(indices[2] - 1));
            } else if (indices[2] < 0) {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(numNormals + indices[2]));
            } else {
                PointAttribute attribute = this.outPointCloud.getAttribute(this.normAttId);
                attribute.setPointMapEntry(vertId, AttributeValueIndex.of(0));
            }
        }

        if (materialAttId >= 0) {
            PointAttribute attribute = this.outPointCloud.getAttribute(this.materialAttId);
            attribute.setPointMapEntry(vertId, AttributeValueIndex.of(lastMaterialId));
        }

        if (subObjAttId >= 0) {
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
