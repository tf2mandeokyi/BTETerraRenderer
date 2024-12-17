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

package com.mndk.bteterrarenderer.draco.compression.mesh.traverser;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;
import lombok.Getter;

public abstract class TraverserBase {

    @Getter private ICornerTable cornerTable = null;
    @Getter private MeshAttributeIndicesEncodingObserver traversalObserver = null;
    private final CppVector<Boolean> isFaceVisited = new CppVector<>(DataType.bool());
    private final CppVector<Boolean> isVertexVisited = new CppVector<>(DataType.bool());

    public void init(ICornerTable cornerTable, MeshAttributeIndicesEncodingObserver traversalObserver) {
        this.cornerTable = cornerTable;
        isFaceVisited.assign(cornerTable.getNumFaces(), false);
        isVertexVisited.assign(cornerTable.getNumVertices(), false);
        this.traversalObserver = traversalObserver;
    }

    public boolean isFaceVisited(FaceIndex faceId) {
        if (faceId.isInvalid()) {
            return true; // Invalid faces are always considered as visited.
        }
        return isFaceVisited.get(faceId.getValue());
    }

    public boolean isFaceVisited(CornerIndex cornerId) {
        if (cornerId.isInvalid()) {
            return true; // Invalid faces are always considered as visited.
        }
        return isFaceVisited.get(cornerId.getValue() / 3);
    }

    public void markFaceVisited(FaceIndex faceId) {
        isFaceVisited.set(faceId.getValue(), true);
    }

    public boolean isVertexVisited(VertexIndex vertId) {
        return isVertexVisited.get(vertId.getValue());
    }

    public void markVertexVisited(VertexIndex vertId) {
        isVertexVisited.set(vertId.getValue(), true);
    }

    public abstract void onTraversalStart();
    public abstract void onTraversalEnd();
    public abstract Status traverseFromCorner(CornerIndex cornerId);

}
