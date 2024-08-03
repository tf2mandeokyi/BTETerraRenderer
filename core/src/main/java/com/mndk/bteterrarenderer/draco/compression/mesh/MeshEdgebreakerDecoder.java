package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.compression.attributes.MeshAttributeIndicesEncodingData;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEdgebreakerConnectivityEncodingMethod;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;
import com.mndk.bteterrarenderer.draco.mesh.MeshAttributeCornerTable;

public class MeshEdgebreakerDecoder extends MeshDecoder {

    private MeshEdgebreakerDecoderImplInterface impl;

    @Override
    public CornerTable getCornerTable() {
        return impl.getCornerTable();
    }

    @Override
    public MeshAttributeCornerTable getAttributeCornerTable(int index) {
        return impl.getAttributeCornerTable(index);
    }

    @Override
    public MeshAttributeIndicesEncodingData getAttributeEncodingData(int index) {
        return impl.getAttributeEncodingData(index);
    }

    @Override
    protected Status createAttributesDecoder(int attDecoderId) {
        return impl.createAttributesDecoder(attDecoderId);
    }

    @Override
    protected Status initializeDecoder() {
        StatusChain chain = new StatusChain();
        Pointer<UByte> traversalDecoderTypeRef = Pointer.newUByte();
        if(this.getBuffer().decode(traversalDecoderTypeRef).isError(chain)) return chain.get();
        MeshEdgebreakerConnectivityEncodingMethod traversalDecoderType =
                MeshEdgebreakerConnectivityEncodingMethod.valueOf(traversalDecoderTypeRef.get());
        if(traversalDecoderType == null) {
            return Status.ioError("Invalid traversal decoder type: " + traversalDecoderTypeRef.get());
        }

        impl = null;
        switch(traversalDecoderType) {
            case STANDARD:
                impl = new MeshEdgebreakerDecoderImpl(new MeshEdgebreakerTraversalDecoder());
                break;
            case PREDICTIVE:
                impl = new MeshEdgebreakerDecoderImpl(new MeshEdgebreakerTraversalPredictiveDecoder());
                break;
            case VALENCE:
                impl = new MeshEdgebreakerDecoderImpl(new MeshEdgebreakerTraversalValenceDecoder());
                break;
        }

        return impl.init(this);
    }

    @Override
    protected Status decodeConnectivity() {
        return impl.decodeConnectivity();
    }

    @Override
    protected Status onAttributesDecoded() {
        return impl.onAttributesDecoded();
    }
}
