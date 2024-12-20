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
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;

import java.util.ArrayList;
import java.util.List;

public class MaxPredictionDegreeTraverser extends TraverserBase {

    private static final int MAX_PRIORITY = 3;

    private final List<CppVector<CornerIndex>> traversalStacks = new ArrayList<>(); {
        for (int i = 0; i < MAX_PRIORITY; ++i) {
            traversalStacks.add(new CppVector<>(CornerIndex.type()));
        }
    }
    private int bestPriority = 0;
    private final IndexTypeVector<VertexIndex, Integer> predictionDegree =
            new IndexTypeVector<>(DataType.int32());

    @Override
    public void onTraversalStart() {
        predictionDegree.resize(this.getCornerTable().getNumVertices(), 0);
    }

    @Override
    public void onTraversalEnd() {}

    @Override
    public Status traverseFromCorner(CornerIndex cornerId) {
        if (predictionDegree.isEmpty()) {
            return Status.ok();
        }

        traversalStacks.get(0).pushBack(cornerId);
        bestPriority = 0;

        VertexIndex nextVert = this.getCornerTable().getVertex(this.getCornerTable().next(cornerId));
        VertexIndex prevVert = this.getCornerTable().getVertex(this.getCornerTable().previous(cornerId));

        if (!this.isVertexVisited(nextVert)) {
            this.markVertexVisited(nextVert);
            this.getTraversalObserver().onNewVertexVisited(nextVert, this.getCornerTable().next(cornerId));
        }
        if (!this.isVertexVisited(prevVert)) {
            this.markVertexVisited(prevVert);
            this.getTraversalObserver().onNewVertexVisited(prevVert, this.getCornerTable().previous(cornerId));
        }
        final VertexIndex tipVertex = this.getCornerTable().getVertex(cornerId);
        if (!this.isVertexVisited(tipVertex)) {
            this.markVertexVisited(tipVertex);
            this.getTraversalObserver().onNewVertexVisited(tipVertex, cornerId);
        }

        while((cornerId = this.popNextCornerToTraverse()).isValid()) {
            FaceIndex faceId = FaceIndex.of(cornerId.getValue() / 3);
            if (this.isFaceVisited(faceId)) {
                continue;
            }

            while (true) {
                faceId = FaceIndex.of(cornerId.getValue() / 3);
                this.markFaceVisited(faceId);
                this.getTraversalObserver().onNewFaceVisited(faceId);

                VertexIndex vertId = this.getCornerTable().getVertex(cornerId);
                if (!this.isVertexVisited(vertId)) {
                    this.markVertexVisited(vertId);
                    this.getTraversalObserver().onNewVertexVisited(vertId, cornerId);
                }

                CornerIndex rightCornerId = this.getCornerTable().getRightCorner(cornerId);
                CornerIndex leftCornerId = this.getCornerTable().getLeftCorner(cornerId);
                FaceIndex rightFaceId = rightCornerId.isInvalid() ?
                        FaceIndex.INVALID : FaceIndex.of(rightCornerId.getValue() / 3);
                FaceIndex leftFaceId = leftCornerId.isInvalid() ?
                        FaceIndex.INVALID : FaceIndex.of(leftCornerId.getValue() / 3);
                boolean isRightFaceVisited = this.isFaceVisited(rightFaceId);
                boolean isLeftFaceVisited = this.isFaceVisited(leftFaceId);

                if (!isLeftFaceVisited) {
                    int priority = this.computePriority(leftCornerId);
                    if (isRightFaceVisited && priority <= bestPriority) {
                        cornerId = leftCornerId;
                        continue;
                    } else {
                        this.addCornerToTraversalStack(leftCornerId, priority);
                    }
                }
                if (!isRightFaceVisited) {
                    int priority = this.computePriority(rightCornerId);
                    if (priority <= bestPriority) {
                        cornerId = rightCornerId;
                        continue;
                    } else {
                        this.addCornerToTraversalStack(rightCornerId, priority);
                    }
                }

                // Couldn't proceed directly to the next corner
                break;
            }
        }
        return Status.ok();
    }

    private CornerIndex popNextCornerToTraverse() {
        for (int i = bestPriority; i < MAX_PRIORITY; ++i) {
            if (!traversalStacks.get(i).isEmpty()) {
                CornerIndex ret = traversalStacks.get(i).back();
                traversalStacks.get(i).popBack();
                bestPriority = i;
                return ret;
            }
        }
        return CornerIndex.INVALID;
    }

    private void addCornerToTraversalStack(CornerIndex ci, int priority) {
        traversalStacks.get(priority).pushBack(ci);
        if (priority < bestPriority) {
            bestPriority = priority;
        }
    }

    private int computePriority(CornerIndex cornerId) {
        VertexIndex vTip = this.getCornerTable().getVertex(cornerId);
        int priority = 0;
        if (!this.isVertexVisited(vTip)) {
            predictionDegree.set(vTip, val -> val + 1);
            int degree = predictionDegree.get(vTip);
            priority = degree > 1 ? 1 : 2;
        }
        return priority;
    }
}
