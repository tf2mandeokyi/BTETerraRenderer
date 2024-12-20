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
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.bitcoder.RAnsBitDecoder;
import com.mndk.bteterrarenderer.draco.core.DecoderBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;

public class MeshEdgebreakerTraversalPredictiveDecoder extends MeshEdgebreakerTraversalDecoder {

    private CornerTable cornerTable = null;
    private int numVertices = 0;
    private final CppVector<Integer> vertexValences = new CppVector<>(DataType.int32());
    private final RAnsBitDecoder predictionDecoder = new RAnsBitDecoder();
    private EdgebreakerTopology lastSymbol = EdgebreakerTopology.INVALID;
    private EdgebreakerTopology predictedSymbol = EdgebreakerTopology.INVALID;

    @Override
    public void init(MeshEdgebreakerDecoderImplInterface decoder) {
        super.init(decoder);
        cornerTable = decoder.getCornerTable();
    }

    @Override
    public void setNumEncodedVertices(int numVertices) {
        this.numVertices = numVertices;
    }

    @Override
    public Status start(DecoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();
        if (super.start(outBuffer).isError(chain)) return chain.get();

        Pointer<Integer> numSplitSymbolsRef = Pointer.newInt();
        if (outBuffer.decode(numSplitSymbolsRef).isError(chain)) return chain.get();
        int numSplitSymbols = numSplitSymbolsRef.get();
        if (numSplitSymbols < 0 || numSplitSymbols >= numVertices) {
            return Status.ioError("Invalid number of split symbols: " + numSplitSymbols + " (numVertices = " + numVertices + ")");
        }

        // Set the valences of all initial vertices to 0.
        vertexValences.resize(numVertices, 0);
        return predictionDecoder.startDecoding(outBuffer);
    }

    @Override
    public EdgebreakerTopology decodeSymbol() {
        // First check if we have a predicted symbol.
        if (predictedSymbol != EdgebreakerTopology.INVALID) {
            // Double check that the predicted symbol was predicted correctly.
            if (predictionDecoder.decodeNextBit()) {
                lastSymbol = predictedSymbol;
                return predictedSymbol;
            }
        }
        // We don't have a predicted symbol or the symbol was mis-predicted.
        // Decode it directly.
        return super.decodeSymbol();
    }

    @Override
    public void newActiveCornerReached(CornerIndex corner) {
        CornerIndex next = cornerTable.next(corner);
        CornerIndex prev = cornerTable.previous(corner);
        // Update valences.
        switch (lastSymbol) {
            case C:
            case S:
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 1);
                break;
            case R:
                vertexValences.set(cornerTable.getVertex(corner).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 2);
                break;
            case L:
                vertexValences.set(cornerTable.getVertex(corner).getValue(), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 1);
                break;
            case E:
                vertexValences.set(cornerTable.getVertex(corner).getValue(), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(next).getValue(), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev).getValue(), val -> val + 2);
                break;
            default:
                break;
        }
        // Compute the new predicted symbol.
        if (lastSymbol == EdgebreakerTopology.C || lastSymbol == EdgebreakerTopology.R) {
            int pivot = cornerTable.getVertex(cornerTable.next(corner)).getValue();
            if (vertexValences.get(pivot) < 6) {
                predictedSymbol = EdgebreakerTopology.R;
            } else {
                predictedSymbol = EdgebreakerTopology.C;
            }
        } else {
            predictedSymbol = EdgebreakerTopology.INVALID;
        }
    }

    @Override
    public void mergeVertices(VertexIndex dest, VertexIndex source) {
        // Update valences on the merged vertices.
        vertexValences.set(dest.getValue(), val -> val + vertexValences.get(source.getValue()));
    }
}
