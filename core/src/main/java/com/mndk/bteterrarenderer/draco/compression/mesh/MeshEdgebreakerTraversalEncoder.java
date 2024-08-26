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
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitEncoder;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import lombok.Getter;
import lombok.Setter;

public class MeshEdgebreakerTraversalEncoder {

    private final RAnsBitEncoder startFaceEncoder = new RAnsBitEncoder();
    private final EncoderBuffer traversalBuffer = new EncoderBuffer();
    @Getter
    private MeshEdgebreakerEncoderImplInterface encoderImpl = null;
    private final CppVector<EdgebreakerTopology> symbols = new CppVector<>(EdgebreakerTopology.BIT_PATTERN_TYPE);
    private RAnsBitEncoder[] attributeConnectivityEncoders = null;
    @Setter
    private int numAttributeData = 0;

    public Status init(MeshEdgebreakerEncoderImplInterface encoder) {
        encoderImpl = encoder;
        return Status.ok();
    }

    public void start() {
        startFaceEncoder.startEncoding();
        if (numAttributeData > 0) {
            attributeConnectivityEncoders = new RAnsBitEncoder[numAttributeData];
            for (int i = 0; i < numAttributeData; ++i) {
                RAnsBitEncoder encoder = new RAnsBitEncoder();
                encoder.startEncoding();
                attributeConnectivityEncoders[i] = encoder;
            }
        }
    }

    public void encodeStartFaceConfiguration(boolean interior) {
        startFaceEncoder.encodeBit(interior);
    }

    public void newCornerReached(CornerIndex corner) {}

    public void encodeSymbol(EdgebreakerTopology symbol) {
        symbols.pushBack(symbol);
    }

    public void encodeAttributeSeam(int attribute, boolean isSeam) {
        attributeConnectivityEncoders[attribute].encodeBit(isSeam);
    }

    public void done() {
        this.encodeTraversalSymbols();
        this.encodeStartFaces();
        this.encodeAttributeSeams();
    }

    public int getNumEncodedSymbols() {
        return (int) symbols.size();
    }

    public EncoderBuffer getBuffer() {
        return traversalBuffer;
    }

    protected void encodeTraversalSymbols() {
        traversalBuffer.startBitEncoding(encoderImpl.getEncoder().getMesh().getNumFaces() * 3L, true);
        for (int i = (int) (symbols.size() - 1); i >= 0; --i) {
            EdgebreakerTopology symbol = symbols.get(i);
            UInt value = UInt.of(symbol.getBitPattern());
            traversalBuffer.encodeLeastSignificantBits32(symbol.getBitPatternLength(), value);
        }
        traversalBuffer.endBitEncoding();
    }

    protected void encodeStartFaces() {
        startFaceEncoder.endEncoding(traversalBuffer);
    }

    protected void encodeAttributeSeams() {
        if (attributeConnectivityEncoders != null) {
            for (int i = 0; i < numAttributeData; ++i) {
                attributeConnectivityEncoders[i].endEncoding(traversalBuffer);
            }
        }
    }

    protected EncoderBuffer getOutputBuffer() {
        return traversalBuffer;
    }

}
