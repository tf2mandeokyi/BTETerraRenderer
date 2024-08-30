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

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;

public interface ICornerTable {

    int getNumVertices();
    int getNumCorners();
    int getNumFaces();
    int getValence(CornerIndex corner);
    int getValence(VertexIndex vertex);
    int getConfidentValence(CornerIndex corner);
    int getConfidentValence(VertexIndex vertex);

    boolean isDegenerated(FaceIndex faceIndex);
    boolean isOnBoundary(VertexIndex vertexIndex);

    CornerIndex next(CornerIndex corner);
    CornerIndex previous(CornerIndex corner);
    CornerIndex opposite(CornerIndex corner);
    CornerIndex swingLeft(CornerIndex corner);
    CornerIndex swingRight(CornerIndex corner);
    CornerIndex getFirstCorner(FaceIndex face);
    CornerIndex getLeftMostCorner(VertexIndex vertex);
    CornerIndex getLeftCorner(CornerIndex cornerId);
    CornerIndex getRightCorner(CornerIndex cornerId);
    CornerIndex[] getAllCorners(FaceIndex face);

    VertexIndex getVertex(CornerIndex corner);
    VertexIndex getConfidentVertex(CornerIndex corner);
    VertexIndex getVertexParent(VertexIndex vertex);

    FaceIndex getFace(CornerIndex corner);
}
