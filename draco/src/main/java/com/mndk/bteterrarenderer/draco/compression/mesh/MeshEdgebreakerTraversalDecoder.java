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

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class MeshEdgebreakerTraversalDecoder {

    @Getter(AccessLevel.PROTECTED)
    private final DecoderBuffer buffer = new DecoderBuffer();
    private final DecoderBuffer symbolBuffer = new DecoderBuffer();
    private final RAnsBitDecoder startFaceDecoder = new RAnsBitDecoder();
    private final DecoderBuffer startFaceBuffer = new DecoderBuffer();
    private RAnsBitDecoder[] attributeConnectivityDecoders = null;
    @Setter
    private int numAttributeData = 0;
    private MeshEdgebreakerDecoderImplInterface decoderImpl = null;

    public void init(MeshEdgebreakerDecoderImplInterface decoder) {
        this.decoderImpl = decoder;
        DecoderBuffer buffer = decoder.getDecoder().getBuffer();
        this.buffer.init(buffer.getDataHead(), buffer.getRemainingSize(), buffer.getBitstreamVersion());
    }

    public int getBitstreamVersion() {
        return decoderImpl.getDecoder().getBitstreamVersion();
    }

    public Status start(DecoderBuffer outBufferRef) {
        StatusChain chain = new StatusChain();

        if (this.decodeTraversalSymbols().isError(chain)) return chain.get();
        if (this.decodeStartFaces().isError(chain)) return chain.get();
        if (this.decodeAttributeSeams().isError(chain)) return chain.get();

        outBufferRef.reset(this.buffer);
        return Status.ok();
    }

    public boolean decodeStartFaceConfiguration() {
        if (buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            Pointer<UInt> faceConfigurationRef = Pointer.newUInt();
            Status status = startFaceBuffer.decodeLeastSignificantBits32(1, faceConfigurationRef);
            if (status.isError()) throw status.getException();
            return faceConfigurationRef.get().intValue() != 0;
        } else {
            return startFaceDecoder.decodeNextBit();
        }
    }

    public EdgebreakerTopology decodeSymbol() {
        Pointer<UInt> symbolRef = Pointer.newUInt();
        symbolBuffer.decodeLeastSignificantBits32(1, symbolRef);
        UInt symbol = symbolRef.get();
        if (EdgebreakerTopology.fromBitPattern(symbol) == EdgebreakerTopology.C) {
            return EdgebreakerTopology.C;
        }
        // Else decode two additional bits.
        Pointer<UInt> symbolSuffixRef = Pointer.newUInt();
        symbolBuffer.decodeLeastSignificantBits32(2, symbolSuffixRef);
        UInt symbolSuffix = symbolSuffixRef.get();
        return EdgebreakerTopology.fromBitPattern(symbol.or(symbolSuffix.shl(1)));
    }

    public void setNumEncodedVertices(int numVertices) {}
    public void newActiveCornerReached(CornerIndex corner) {}
    public void mergeVertices(VertexIndex dest, VertexIndex source) {}

    public boolean decodeAttributeSeam(int attribute) {
        return attributeConnectivityDecoders[attribute].decodeNextBit();
    }

    public void done() {
        if (symbolBuffer.isBitDecoderActive()) {
            symbolBuffer.endBitDecoding();
        }
        if (buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            startFaceBuffer.endBitDecoding();
        } else {
            startFaceDecoder.endDecoding();
        }
    }

    protected Status decodeTraversalSymbols() {
        StatusChain chain = new StatusChain();

        Pointer<ULong> traversalSizeRef = Pointer.newULong();
        this.symbolBuffer.reset(this.buffer);
        if (symbolBuffer.startBitDecoding(true, traversalSizeRef).isError(chain)) return chain.get();
        ULong traversalSize = traversalSizeRef.get();
        this.buffer.reset(this.symbolBuffer);
        if (traversalSize.gt(this.buffer.getRemainingSize())) {
            return Status.ioError("Traversal size is larger than remaining buffer size.");
        }
        this.buffer.advance(traversalSize.longValue());
        return Status.ok();
    }

    public Status decodeStartFaces() {
        StatusChain chain = new StatusChain();

        if (buffer.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            this.startFaceBuffer.reset(this.buffer);
            Pointer<ULong> traversalSizeRef = Pointer.newULong();
            if (startFaceBuffer.startBitDecoding(true, traversalSizeRef).isError(chain)) return chain.get();
            ULong traversalSize = traversalSizeRef.get();
            this.buffer.reset(this.startFaceBuffer);
            if (traversalSize.gt(this.buffer.getRemainingSize())) {
                return Status.ioError("Traversal size is larger than remaining buffer size.");
            }
            this.buffer.advance(traversalSize.longValue());
            return Status.ok();
        }
        return this.startFaceDecoder.startDecoding(this.buffer);
    }

    public Status decodeAttributeSeams() {
        StatusChain chain = new StatusChain();

        if (numAttributeData > 0) {
            attributeConnectivityDecoders = new RAnsBitDecoder[numAttributeData];
            for (int i = 0; i < numAttributeData; ++i) {
                RAnsBitDecoder decoder = new RAnsBitDecoder();
                if (decoder.startDecoding(buffer).isError(chain)) return chain.get();
                attributeConnectivityDecoders[i] = decoder;
            }
        }
        return Status.ok();
    }
}
