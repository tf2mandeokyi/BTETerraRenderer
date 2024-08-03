package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;
import com.mndk.bteterrarenderer.draco.pointcloud.PointCloud;

public interface MeshEncodingDataSource {
    Mesh getMesh();
    PointCloud getPointCloud();
    EncodedGeometryType getGeometryType();
    CornerTable getCornerTable();
    MeshAttributeCornerTable getAttributeCornerTable(int attId);
    MeshAttributeIndicesEncodingData getAttributeEncodingData(int attId);
}
