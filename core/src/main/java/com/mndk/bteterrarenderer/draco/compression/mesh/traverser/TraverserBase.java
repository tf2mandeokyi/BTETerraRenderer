package com.mndk.bteterrarenderer.draco.compression.mesh.traverser;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
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
        if(faceId.isInvalid()) {
            return true; // Invalid faces are always considered as visited.
        }
        return isFaceVisited.get(faceId.getValue());
    }

    public boolean isFaceVisited(CornerIndex cornerId) {
        if(cornerId.isInvalid()) {
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
