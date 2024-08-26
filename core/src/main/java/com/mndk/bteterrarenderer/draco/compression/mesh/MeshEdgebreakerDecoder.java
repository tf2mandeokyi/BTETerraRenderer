/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        MeshEdgebreakerTraversalDecoder traversalDecoder = null;
        switch(traversalDecoderType) {
            case STANDARD: traversalDecoder = new MeshEdgebreakerTraversalDecoder(); break;
            case PREDICTIVE: traversalDecoder = new MeshEdgebreakerTraversalPredictiveDecoder(); break;
            case VALENCE: traversalDecoder = new MeshEdgebreakerTraversalValenceDecoder(); break;
        }
        impl = new MeshEdgebreakerDecoderImpl(traversalDecoder);
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
