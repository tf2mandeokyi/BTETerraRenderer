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

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.draco.attributes.FaceIndex;
import com.mndk.bteterrarenderer.draco.compression.attributes.AttributesEncoder;
import com.mndk.bteterrarenderer.draco.compression.attributes.LinearSequencer;
import com.mndk.bteterrarenderer.draco.compression.attributes.SequentialAttributeEncodersController;
import com.mndk.bteterrarenderer.draco.compression.config.MeshEncoderMethod;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolEncoding;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.Mesh;

public class MeshSequentialEncoder extends MeshEncoder {

    @Override
    public UByte getEncodingMethod() {
        return UByte.of(MeshEncoderMethod.SEQUENTIAL.getValue());
    }

    @Override
    protected Status encodeConnectivity() {
        StatusChain chain = new StatusChain();

        // Serialize indices.
        int numFaces = this.getMesh().getNumFaces();
        EncoderBuffer buffer = this.getBuffer();
        if (buffer.encodeVarint(UInt.of(numFaces)).isError(chain)) return chain.get();
        if (buffer.encodeVarint(UInt.of(this.getMesh().getNumPoints())).isError(chain)) return chain.get();

        // We encode all attributes in the original (possibly duplicated) format.
        if (this.getOptions().getGlobalBool("compress_connectivity", false)) {
            // 0 = Encode compressed indices.
            if (buffer.encode(UByte.of(0)).isError(chain)) return chain.get();
            if (this.compressAndEncodeIndices().isError(chain)) return chain.get();
        } else {
            // 1 = Encode indices directly.
            if (buffer.encode(UByte.of(1)).isError(chain)) return chain.get();
            // Store vertex indices using a smallest data type that fits their range.
            if (this.getMesh().getNumPoints() < 256) {
                // Serialize indices as uint8_t.
                for (FaceIndex i : FaceIndex.range(0, numFaces)) {
                    Mesh.Face face = this.getMesh().getFace(i);
                    if (buffer.encode(UByte.of(face.getValue(0))).isError(chain)) return chain.get();
                    if (buffer.encode(UByte.of(face.getValue(1))).isError(chain)) return chain.get();
                    if (buffer.encode(UByte.of(face.getValue(2))).isError(chain)) return chain.get();
                }
            } else if (this.getMesh().getNumPoints() < (1 << 16)) {
                // Serialize indices as uint16_t.
                for (FaceIndex i : FaceIndex.range(0, numFaces)) {
                    Mesh.Face face = this.getMesh().getFace(i);
                    if (buffer.encode(UShort.of(face.getValue(0))).isError(chain)) return chain.get();
                    if (buffer.encode(UShort.of(face.getValue(1))).isError(chain)) return chain.get();
                    if (buffer.encode(UShort.of(face.getValue(2))).isError(chain)) return chain.get();
                }
            } else if (this.getMesh().getNumPoints() < (1 << 21)) {
                // Serialize indices as varint.
                for (FaceIndex i : FaceIndex.range(0, numFaces)) {
                    Mesh.Face face = this.getMesh().getFace(i);
                    if (buffer.encodeVarint(UInt.of(face.getValue(0))).isError(chain)) return chain.get();
                    if (buffer.encodeVarint(UInt.of(face.getValue(1))).isError(chain)) return chain.get();
                    if (buffer.encodeVarint(UInt.of(face.getValue(2))).isError(chain)) return chain.get();
                }
            } else {
                // Serialize faces as uint32_t (default).
                for (FaceIndex i : FaceIndex.range(0, numFaces)) {
                    Mesh.Face face = this.getMesh().getFace(i);
                    if (buffer.encode(UInt.of(face.getValue(0))).isError(chain)) return chain.get();
                    if (buffer.encode(UInt.of(face.getValue(1))).isError(chain)) return chain.get();
                    if (buffer.encode(UInt.of(face.getValue(2))).isError(chain)) return chain.get();
                }
            }
        }
        return Status.ok();
    }

    @Override
    protected Status generateAttributesEncoder(int attId) {
        // Create only one attribute encoder that is going to encode all points in a
        // linear sequence.
        if (attId == 0) {
            // Create a new attribute encoder only for the first attribute.
            LinearSequencer sequencer = new LinearSequencer(this.getPointCloud().getNumPoints());
            AttributesEncoder attributesEncoder = new SequentialAttributeEncodersController(sequencer, attId);
            this.addAttributesEncoder(attributesEncoder);
        } else {
            // Reuse the existing attribute encoder for other attributes.
            this.getAttributesEncoder(0).addAttributeId(attId);
        }
        return Status.ok();
    }

    @Override
    protected void computeNumberOfEncodedPoints() {
        this.setNumEncodedPoints(this.getMesh().getNumPoints());
    }

    @Override
    protected void computeNumberOfEncodedFaces() {
        this.setNumEncodedFaces(this.getMesh().getNumFaces());
    }

    private Status compressAndEncodeIndices() {
        CppVector<UInt> indicesBuffer = new CppVector<>(DataType.uint32());
        int lastIndexValue = 0;
        int numFaces = this.getMesh().getNumFaces();
        for (FaceIndex i : FaceIndex.range(0, numFaces)) {
            Mesh.Face face = this.getMesh().getFace(i);
            for (int j = 0; j < 3; ++j) {
                int indexValue = face.getValue(j);
                int indexDiff = indexValue - lastIndexValue;
                int encodedVal = (Math.abs(indexDiff) << 1) | (indexDiff < 0 ? 1 : 0);
                indicesBuffer.pushBack(UInt.of(encodedVal));
                lastIndexValue = indexValue;
            }
        }
        SymbolEncoding.encode(indicesBuffer.getPointer(), (int) indicesBuffer.size(), 1, null,
                this.getBuffer());
        return Status.ok();
    }
}
