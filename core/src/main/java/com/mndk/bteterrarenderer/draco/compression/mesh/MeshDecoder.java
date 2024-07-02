package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.config.DecoderOptions;
import com.mndk.bteterrarenderer.draco.compression.config.EncodedGeometryType;
import com.mndk.bteterrarenderer.draco.compression.pointcloud.PointCloudDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;
import lombok.Getter;

@Getter
public abstract class MeshDecoder extends PointCloudDecoder {

    private Mesh mesh;

    public MeshDecoder() {
        this.mesh = null;
    }

    public EncodedGeometryType getGeometryType() {
        return EncodedGeometryType.TRIANGULAR_MESH;
    }

    public Status decode(DecoderOptions options, DecoderBuffer inBuffer, Mesh outMesh) {
        this.mesh = outMesh;
        return super.decode(options, inBuffer, outMesh);
    }

    public CornerTable getCornerTable() {
        return null;
    }

    public MeshAttributeCornerTable getAttributeCornerTable(int index) {
        return null;
    }

    public MeshAttributeIndicesEncodingData getAttributeEncodingData(int index) {
        return null;
    }

    protected Status decodeGeometryData() {
        StatusChain chain = new StatusChain();

        if(mesh == null) {
            return Status.dracoError("Mesh is not initialized.");
        }
        if(decodeConnectivity().isError(chain)) return chain.get();
        return super.decodeGeometryData();
    }

    protected abstract Status decodeConnectivity();
}
