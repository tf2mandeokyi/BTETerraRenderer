package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;

public interface MeshEdgebreakerDecoderImplInterface {

    Status init(MeshEdgebreakerDecoder decoder);

    MeshAttributeCornerTable getAttributeCornerTable(int attId);
    MeshAttributeIndicesEncodingData getAttributeEncodingData(int attId);
    Status createAttributesDecoder(int attDecoderId);
    Status decodeConnectivity();
    Status onAttributesDecoded();

    MeshEdgebreakerDecoder getDecoder();
    CornerTable getCornerTable();

}
