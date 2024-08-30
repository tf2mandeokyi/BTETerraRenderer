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

package com.mndk.bteterrarenderer.draco.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.DracoDataType;
import com.mndk.bteterrarenderer.draco.metadata.AttributeMetadata;
import com.mndk.bteterrarenderer.draco.metadata.GeometryMetadata;

public class TriangleSoupMeshBuilder {

    private final CppVector<Byte> attributeElementTypes = new CppVector<>(DataType.int8());
    private Mesh mesh;

    public void start(int numFaces) {
        mesh = new Mesh();
        mesh.setNumFaces(numFaces);
        mesh.setNumPoints(numFaces * 3);
        attributeElementTypes.clear();
    }

    public int addAttribute(GeometryAttribute.Type attributeType, byte numComponents, DracoDataType dataType) {
        return addAttribute(attributeType, numComponents, dataType, false);
    }
    public int addAttribute(GeometryAttribute.Type attributeType, byte numComponents, DracoDataType dataType, boolean normalized) {
        GeometryAttribute attribute = new GeometryAttribute();
        attribute.init(attributeType, null, numComponents, dataType, normalized);
        attributeElementTypes.pushBack((byte) -1);
        return mesh.addAttribute(attribute, true, mesh.getNumPoints());
    }

    public <T> void setAttributeValuesForFace(int attId, FaceIndex faceId, Pointer<T> cornerValue0,
                                              Pointer<T> cornerValue1, Pointer<T> cornerValue2) {
        int startIndex = 3 * faceId.getValue();
        PointAttribute att = mesh.getAttribute(attId);
        att.setAttributeValue(AttributeValueIndex.of(startIndex), cornerValue0);
        att.setAttributeValue(AttributeValueIndex.of(startIndex + 1), cornerValue1);
        att.setAttributeValue(AttributeValueIndex.of(startIndex + 2), cornerValue2);
        mesh.setFace(faceId, new Mesh.Face(
                PointIndex.of(startIndex), PointIndex.of(startIndex + 1), PointIndex.of(startIndex + 2)
        ));
        attributeElementTypes.set(attId, (byte) MeshAttributeElementType.CORNER.getValue());
    }

    public <T> void setPerFaceAttributeValueForFace(int attId, FaceIndex faceId, Pointer<T> value) {
        int startIndex = 3 * faceId.getValue();
        PointAttribute att = mesh.getAttribute(attId);
        att.setAttributeValue(AttributeValueIndex.of(startIndex), value);
        att.setAttributeValue(AttributeValueIndex.of(startIndex + 1), value);
        att.setAttributeValue(AttributeValueIndex.of(startIndex + 2), value);
        mesh.setFace(faceId, new Mesh.Face(
                PointIndex.of(startIndex), PointIndex.of(startIndex + 1), PointIndex.of(startIndex + 2)
        ));
        if(attributeElementTypes.get(attId) < 0) {
            attributeElementTypes.set(attId, (byte) MeshAttributeElementType.FACE.getValue());
        }
    }

    public void addMetadata(GeometryMetadata metadata) {
        mesh.addMetadata(metadata);
    }

    public void setAttributeUniqueId(int attId, UInt uniqueId) {
        mesh.getAttribute(attId).setUniqueId(uniqueId);
    }

    public void addAttributeMetadata(int attId, AttributeMetadata metadata) {
        mesh.addAttributeMetadata(attId, metadata);
    }

    public Mesh finalizeMesh() {
        if(mesh.deduplicateAttributeValues().isError()) {
            return null;
        }
        mesh.deduplicatePointIds();
        for(int i = 0; i < attributeElementTypes.size(); ++i) {
            if(attributeElementTypes.get(i) >= 0) {
                mesh.setAttributeElementType(i, MeshAttributeElementType.valueOf(attributeElementTypes.get(i)));
            }
        }
        return mesh;
    }

}
