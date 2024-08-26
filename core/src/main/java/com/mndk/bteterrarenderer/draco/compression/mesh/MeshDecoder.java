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

public abstract class MeshDecoder extends PointCloudDecoder implements MeshEncodingDataSource {

    private Mesh mesh;

    public MeshDecoder() {
        this.mesh = null;
    }

    @Override public Mesh getMesh() { return mesh; }
    @Override public EncodedGeometryType getGeometryType() { return EncodedGeometryType.TRIANGULAR_MESH; }
    @Override public CornerTable getCornerTable() { return null; }
    @Override public MeshAttributeCornerTable getAttributeCornerTable(int index) { return null; }
    @Override public MeshAttributeIndicesEncodingData getAttributeEncodingData(int index) { return null; }

    public Status decode(DecoderOptions options, DecoderBuffer inBuffer, Mesh outMesh) {
        this.mesh = outMesh;
        return super.decode(options, inBuffer, outMesh);
    }

    protected Status decodeGeometryData() {
        StatusChain chain = new StatusChain();

        if(mesh == null) return Status.dracoError("Mesh is not initialized.");
        if(decodeConnectivity().isError(chain)) return chain.get();
        return super.decodeGeometryData();
    }

    protected abstract Status decodeConnectivity();
}
