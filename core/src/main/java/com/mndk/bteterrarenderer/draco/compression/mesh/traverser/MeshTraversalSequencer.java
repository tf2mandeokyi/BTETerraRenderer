package com.mndk.bteterrarenderer.draco.compression.mesh.traverser;

import com.mndk.bteterrarenderer.draco.attributes.*;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.attributes.PointsSequencer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
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
        for(FaceIndex f : FaceIndex.range(0, numFaces)) {
            Mesh.Face face = mesh.getFace(f);
            for(int p = 0; p < 3; ++p) {
                PointIndex pointId = face.get(p);
                VertexIndex vertId = cornerTable.getVertex(CornerIndex.of(3 * f.getValue() + p));
                if(vertId.isInvalid()) {
                    return Status.ioError("Invalid vertex index");
                }
                AttributeValueIndex attEntryId = AttributeValueIndex.of(
                        encodingData.getVertexToEncodedAttributeValueIndexMap().get(vertId.getValue()));
                if(pointId.getValue() >= numPoints || attEntryId.getValue() >= numPoints) {
                    // There cannot be more attribute values than the number of points.
                    return Status.ioError("Invalid point index: " + pointId + " or attribute value index: " + attEntryId);
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
        if(cornerOrder != null) {
            for(CornerIndex corner : cornerOrder) {
                if(this.processCorner(corner).isError(chain)) return chain.get();
            }
        } else {
            int numFaces = traverser.getCornerTable().getNumFaces();
            for(int i = 0; i < numFaces; ++i) {
                CornerIndex cornerId = CornerIndex.of(3 * i);
                if(this.processCorner(cornerId).isError(chain)) return chain.get();
            }
        }
        traverser.onTraversalEnd();
        return Status.ok();
    }

    private Status processCorner(CornerIndex cornerId) {
        return traverser.traverseFromCorner(cornerId);
    }
}
