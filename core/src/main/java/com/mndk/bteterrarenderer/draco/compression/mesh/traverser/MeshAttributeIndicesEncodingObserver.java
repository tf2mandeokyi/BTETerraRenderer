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
