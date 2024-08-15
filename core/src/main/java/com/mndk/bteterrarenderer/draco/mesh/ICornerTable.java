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
