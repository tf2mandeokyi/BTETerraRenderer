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
