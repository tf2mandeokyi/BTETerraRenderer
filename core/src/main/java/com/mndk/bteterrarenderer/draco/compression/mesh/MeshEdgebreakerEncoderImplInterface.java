package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;

public interface MeshEdgebreakerEncoderImplInterface {
    Status init(MeshEdgebreakerEncoder encoder);

    MeshAttributeCornerTable getAttributeCornerTable(int attId);
    MeshAttributeIndicesEncodingData getAttributeEncodingData(int attId);
    Status generateAttributesEncoder(int attId);
    Status encodeAttributesEncoderIdentifier(int attEncoderId);
    Status encodeConnectivity();

    CornerTable getCornerTable();

    boolean isFaceEncoded(int fi);

    MeshEdgebreakerEncoder getEncoder();
}
