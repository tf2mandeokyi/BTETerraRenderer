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
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;

import java.util.ArrayList;
import java.util.List;

public class MeshConnectedComponents {

    public static class ConnectedComponent {
        private final CppVector<Integer> vertices = new CppVector<>(DataType.int32());
        private final CppVector<Integer> faces = new CppVector<>(DataType.int32());
        private final CppVector<Integer> boundaryEdges = new CppVector<>(DataType.int32());
    }

    private final CppVector<Integer> vertexToComponentMap = new CppVector<>(DataType.int32());
    private final CppVector<Integer> faceToComponentMap = new CppVector<>(DataType.int32());
    private final CppVector<Integer> boundaryCornerToComponentMap = new CppVector<>(DataType.int32());
    private final List<ConnectedComponent> components = new ArrayList<>();

    public void findConnectedComponents(ICornerTable cornerTable) {
        components.clear();
        vertexToComponentMap.assign(cornerTable.getNumVertices(), -1);
        faceToComponentMap.assign(cornerTable.getNumFaces(), -1);
        boundaryCornerToComponentMap.assign(cornerTable.getNumCorners(), -1);
        CppVector<Boolean> isFaceVisited = new CppVector<>(DataType.bool(), cornerTable.getNumFaces(), false);
        CppVector<Integer> faceStack = new CppVector<>(DataType.int32());

        for (int faceId = 0; faceId < cornerTable.getNumFaces(); ++faceId) {
            if (isFaceVisited.get(faceId)) continue;
            if (cornerTable.isDegenerated(FaceIndex.of(faceId))) continue;

            int componentId = components.size();
            components.add(new ConnectedComponent());
            faceStack.pushBack(faceId);
            isFaceVisited.set(faceId, true);
            while (!faceStack.isEmpty()) {
                int actFaceId = faceStack.popBack();
                if (faceToComponentMap.get(actFaceId) == -1) {
                    faceToComponentMap.set(actFaceId, componentId);
                    components.get(componentId).faces.pushBack(actFaceId);
                }
                CornerIndex[] corners = cornerTable.getAllCorners(FaceIndex.of(actFaceId));
                for (int c = 0; c < 3; ++c) {
                    int vertexId = cornerTable.getVertex(corners[c]).getValue();
                    if (vertexToComponentMap.get(vertexId) == -1) {
                        vertexToComponentMap.set(vertexId, componentId);
                        components.get(componentId).vertices.pushBack(vertexId);
                    }
                    CornerIndex oppCorner = cornerTable.opposite(corners[c]);
                    if (oppCorner.isInvalid()) {
                        if (boundaryCornerToComponentMap.get(corners[c].getValue()) == -1) {
                            boundaryCornerToComponentMap.set(corners[c].getValue(), componentId);
                            components.get(componentId).boundaryEdges.pushBack(corners[c].getValue());
                        }
                        continue;
                    }

                    int oppFaceId = cornerTable.getFace(oppCorner).getValue();
                    if (isFaceVisited.get(oppFaceId)) continue;
                    isFaceVisited.set(oppFaceId, true);
                    faceStack.pushBack(oppFaceId);
                }
            }
        }
    }

    public int getNumConnectedComponents() { return components.size(); }
    public ConnectedComponent getConnectedComponent(int index) { return components.get(index); }

    public int getConnectedComponentIdAtVertex(int vertexId) { return vertexToComponentMap.get(vertexId); }
    public int getNumConnectedComponentVertices(int componentId) { return (int) components.get(componentId).vertices.size(); }
    public int getConnectedComponentVertex(int componentId, int i) { return components.get(componentId).vertices.get(i); }
    public int getConnectedComponentIdAtFace(int faceId) { return faceToComponentMap.get(faceId); }
    public int getNumConnectedComponentFaces(int componentId) { return (int) components.get(componentId).faces.size(); }
    public int getConnectedComponentFace(int componentId, int i) { return components.get(componentId).faces.get(i); }
    public int getNumConnectedComponentBoundaryEdges(int componentId) { return (int) components.get(componentId).boundaryEdges.size(); }
    public int getConnectedComponentBoundaryEdge(int componentId, int i) { return components.get(componentId).boundaryEdges.get(i); }

}
