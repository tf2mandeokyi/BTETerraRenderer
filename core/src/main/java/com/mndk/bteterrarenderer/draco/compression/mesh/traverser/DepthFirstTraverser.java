package com.mndk.bteterrarenderer.draco.compression.mesh.traverser;

import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

public class DepthFirstTraverser extends TraverserBase {

    private final CppVector<CornerIndex> cornerTraversalStack = new CppVector<>(CornerIndex.type());

    @Override
    public void onTraversalStart() {}

    @Override
    public void onTraversalEnd() {}

    @Override
    public Status traverseFromCorner(CornerIndex cornerId) {
        if(this.isFaceVisited(cornerId)) {
            return Status.ok(); // Already traversed.
        }

        cornerTraversalStack.clear();
        cornerTraversalStack.pushBack(cornerId);

        // Check the remaining corners as they may not be processed yet.
        VertexIndex nextVert = this.getCornerTable().getVertex(this.getCornerTable().next(cornerId));
        VertexIndex prevVert = this.getCornerTable().getVertex(this.getCornerTable().previous(cornerId));
        if (nextVert.isInvalid()) {
            return Status.ioError("Invalid next vertex: " + nextVert);
        } else if (prevVert.isInvalid()) {
            return Status.ioError("Invalid previous vertex: " + prevVert);
        }
        if(!this.isVertexVisited(nextVert)) {
            this.markVertexVisited(nextVert);
            this.getTraversalObserver().onNewVertexVisited(nextVert, this.getCornerTable().next(cornerId));
        }
        if(!this.isVertexVisited(prevVert)) {
            this.markVertexVisited(prevVert);
            this.getTraversalObserver().onNewVertexVisited(prevVert, this.getCornerTable().previous(cornerId));
        }

        // Start the actual traversal.
        while(!cornerTraversalStack.isEmpty()) {
            // Currently processed corner.
            cornerId = cornerTraversalStack.popBack();
            FaceIndex faceId = FaceIndex.of(cornerId.getValue() / 3);
            // Make sure the face hasn't been visited yet.
            if(cornerId.isInvalid() || this.isFaceVisited(faceId)) {
                // This face has been already traversed.
                continue;
            }
            while(true) {
                this.markFaceVisited(faceId);
                this.getTraversalObserver().onNewFaceVisited(faceId);
                VertexIndex vertId = this.getCornerTable().getVertex(cornerId);
                if(vertId.isInvalid()) {
                    return Status.ioError("Invalid vertex index: " + vertId);
                }
                if(!this.isVertexVisited(vertId)) {
                    boolean onBoundary = this.getCornerTable().isOnBoundary(vertId);
                    this.markVertexVisited(vertId);
                    this.getTraversalObserver().onNewVertexVisited(vertId, cornerId);
                    if(!onBoundary) {
                        cornerId = this.getCornerTable().getRightCorner(cornerId);
                        faceId = FaceIndex.of(cornerId.getValue() / 3);
                        continue;
                    }
                }
                // The current vertex has been already visited or it was on a boundary.
                // We need to determine whether we can visit any of it's neighboring faces.
                CornerIndex rightCornerId = this.getCornerTable().getRightCorner(cornerId);
                CornerIndex leftCornerId = this.getCornerTable().getLeftCorner(cornerId);
                FaceIndex rightFaceId = rightCornerId.isInvalid() ?
                        FaceIndex.INVALID : FaceIndex.of(rightCornerId.getValue() / 3);
                FaceIndex leftFaceId = leftCornerId.isInvalid() ?
                        FaceIndex.INVALID : FaceIndex.of(leftCornerId.getValue() / 3);
                if(this.isFaceVisited(rightFaceId)) {
                    // Right face has been already visited.
                    if(this.isFaceVisited(leftFaceId)) {
                        // Both neighboring faces are visited. End reached.
                        break; // Break from the while (true) loop.
                    } else {
                        // Go to the left face.
                        cornerId = leftCornerId;
                        faceId = leftFaceId;
                    }
                } else {
                    // Right face was not visited.
                    if (this.isFaceVisited(leftFaceId)) {
                        // Left face visited, go to the right one.
                        cornerId = rightCornerId;
                        faceId = rightFaceId;
                    } else {
                        // Both neighboring faces are unvisited, we need to visit both of them.

                        // Split the traversal.
                        // First make the top of the current corner stack point to the left
                        // face (this one will be processed second).
                        cornerTraversalStack.pushBack(leftCornerId);
                        // Add a new corner to the top of the stack (right face needs to
                        // be traversed first).
                        cornerTraversalStack.pushBack(rightCornerId);
                        // Break from the while (true) loop.
                        break;
                    }
                }
            }
        }
        return Status.ok();
    }
}
