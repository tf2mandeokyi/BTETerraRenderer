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

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.PointIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.attributes.PointsSequencer;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MeshAttributeIndicesEncodingObserver {

    private ICornerTable attConnectivity;
    private Mesh mesh;
    private PointsSequencer sequencer;
    private MeshAttributeIndicesEncodingData encodingData;

    public void init(ICornerTable connectivity, Mesh mesh, PointsSequencer sequencer,
                     MeshAttributeIndicesEncodingData encodingData) {
        this.attConnectivity = connectivity;
        this.mesh = mesh;
        this.sequencer = sequencer;
        this.encodingData = encodingData;
    }

    public void onNewFaceVisited(FaceIndex face) {}

    public void onNewVertexVisited(VertexIndex vertex, CornerIndex corner) {
        PointIndex pointId = mesh.getFace(FaceIndex.of(corner.getValue() / 3)).get(corner.getValue() % 3);
        sequencer.addPointId(pointId);
        encodingData.getEncodedAttributeValueIndexToCornerMap().pushBack(corner);
        encodingData.getVertexToEncodedAttributeValueIndexMap().set(vertex.getValue(), encodingData.getNumValues());
        encodingData.setNumValues(encodingData.getNumValues() + 1);
    }

}
