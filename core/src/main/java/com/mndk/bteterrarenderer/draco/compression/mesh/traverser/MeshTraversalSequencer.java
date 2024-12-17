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

import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.attributes.PointsSequencer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.ICornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import lombok.Setter;

public class MeshTraversalSequencer extends PointsSequencer {

    @Setter
    private TraverserBase traverser;
    private final Mesh mesh;
    private final MeshAttributeIndicesEncodingData encodingData;
    @Setter
    private CppVector<CornerIndex> cornerOrder = null;

    public MeshTraversalSequencer(Mesh mesh, MeshAttributeIndicesEncodingData encodingData) {
        this.mesh = mesh;
        this.encodingData = encodingData;
    }

    @Override
    public Status updatePointToAttributeIndexMapping(PointAttribute attribute) {
        ICornerTable cornerTable = traverser.getCornerTable();
        attribute.setExplicitMapping(mesh.getNumPoints());
        int numFaces = mesh.getNumFaces();
        int numPoints = mesh.getNumPoints();
        for (FaceIndex f : FaceIndex.range(0, numFaces)) {
            Mesh.Face face = mesh.getFace(f);
            for (int p = 0; p < 3; ++p) {
                PointIndex pointId = face.get(p);
                VertexIndex vertId = cornerTable.getVertex(CornerIndex.of(3 * f.getValue() + p));
                if (vertId.isInvalid()) {
                    return Status.ioError("Invalid vertex index");
                }
                AttributeValueIndex attEntryId = AttributeValueIndex.of(
                        encodingData.getVertexToEncodedAttributeValueIndexMap().get(vertId.getValue()));
                if (pointId.getValue() >= numPoints) {
                    // There cannot be more attribute values than the number of points.
                    return Status.ioError("Invalid point index " + pointId + ": should be less than " + numPoints);
                } else if (attEntryId.getValue() >= numPoints) {
                    // There cannot be more attribute values than the number of points.
                    return Status.ioError("Invalid point index " + pointId + ": should be less than " + numPoints);
                }
                attribute.setPointMapEntry(pointId, attEntryId);
            }
        }
        return Status.ok();
    }

    @Override
    public Status generateSequenceInternal() {
        StatusChain chain = new StatusChain();

        this.getOutPointIds().reserve(traverser.getCornerTable().getNumVertices());

        traverser.onTraversalStart();
        if (cornerOrder != null) {
            for (CornerIndex cornerIndex : cornerOrder) {
                if (this.processCorner(cornerIndex).isError(chain)) return chain.get();
            }
        } else {
            int numFaces = traverser.getCornerTable().getNumFaces();
            for (int i = 0; i < numFaces; ++i) {
                if (this.processCorner(CornerIndex.of(3 * i)).isError(chain)) return chain.get();
            }
        }
        traverser.onTraversalEnd();
        return Status.ok();
    }

    private Status processCorner(CornerIndex cornerId) {
        return traverser.traverseFromCorner(cornerId);
    }
}
