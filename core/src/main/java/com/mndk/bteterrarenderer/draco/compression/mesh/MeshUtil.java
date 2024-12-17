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

package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeshUtil {

    public CornerTable createCornerTableFromPositionAttribute(Mesh mesh) {
        return createCornerTableFromAttribute(mesh, GeometryAttribute.Type.POSITION);
    }

    public CornerTable createCornerTableFromAttribute(Mesh mesh, GeometryAttribute.Type type) {
        PointAttribute att = mesh.getNamedAttribute(type);
        if (att == null) return null;
        IndexTypeVector<FaceIndex, CornerTable.FaceType> faces =
                new IndexTypeVector<>(CornerTable.FaceType::new, mesh.getNumFaces());
        for (FaceIndex i : FaceIndex.range(0, mesh.getNumFaces())) {
            CornerTable.FaceType newFace = new CornerTable.FaceType();
            Mesh.Face face = mesh.getFace(i);
            for (int j = 0; j < 3; ++j) {
                newFace.set(j, VertexIndex.of(att.getMappedIndex(face.get(j)).getValue()));
            }
            faces.set(i, newFace);
        }
        return CornerTable.create(faces).getValue();
    }

    public CornerTable createCornerTableFromAllAttributes(Mesh mesh) {
        IndexTypeVector<FaceIndex, CornerTable.FaceType> faces =
                new IndexTypeVector<>(CornerTable.FaceType::new, mesh.getNumFaces());
        for (FaceIndex i : FaceIndex.range(0, mesh.getNumFaces())) {
            CornerTable.FaceType newFace = new CornerTable.FaceType();
            Mesh.Face face = mesh.getFace(i);
            for (int j = 0; j < 3; ++j) {
                newFace.set(j, VertexIndex.of(face.get(j).getValue()));
            }
            faces.set(i, newFace);
        }
        return CornerTable.create(faces).getValue();
    }

}
