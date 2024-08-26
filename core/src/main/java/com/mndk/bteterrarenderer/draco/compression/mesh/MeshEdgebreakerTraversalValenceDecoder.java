package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolDecoding;
import com.mndk.bteterrarenderer.draco.core.*;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;

import java.util.ArrayList;
import java.util.List;

public class MeshEdgebreakerTraversalValenceDecoder extends MeshEdgebreakerTraversalDecoder {

    private CornerTable cornerTable = null;
    private int numVertices = 0;
    private final IndexTypeVector<VertexIndex, Integer> vertexValences =
            new IndexTypeVector<>(DataType.int32());
    private EdgebreakerTopology lastSymbol = EdgebreakerTopology.INVALID;
    private int activeContext = -1;

    private int minValence = 2;
    private int maxValence = 7;
    private final List<CppVector<UInt>> contextSymbols = new ArrayList<>();
    private final CppVector<Integer> contextCounters = new CppVector<>(DataType.int32());

    @Override
    public void init(MeshEdgebreakerDecoderImplInterface decoder) {
        super.init(decoder);
        this.cornerTable = decoder.getCornerTable();
    }

    @Override
    public void setNumEncodedVertices(int numVertices) {
        this.numVertices = numVertices;
    }

    @Override
    public Status start(DecoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            if(super.decodeTraversalSymbols().isError(chain)) return chain.get();
        }
        if(super.decodeStartFaces().isError(chain)) return chain.get();
        if(super.decodeAttributeSeams().isError(chain)) return chain.get();
        outBuffer.reset(this.getBuffer());

        if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            Pointer<UInt> numSplitSymbols = Pointer.newUInt();
            if(this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 0)) {
                if(outBuffer.decode(numSplitSymbols).isError(chain)) return chain.get();
            } else {
                if(outBuffer.decodeVarint(numSplitSymbols).isError(chain)) return chain.get();
            }
            UInt numSplitSymbolsValue = numSplitSymbols.get();
            if(numSplitSymbolsValue.ge(numVertices)) {
                return Status.dracoError("Invalid number of split symbols");
            }

            Pointer<Byte> modeRef = Pointer.newByte();
            if(outBuffer.decode(modeRef).isError(chain)) return chain.get();
            byte mode = modeRef.get();
            if(mode == Edgebreaker.EDGEBREAKER_VALENCE_MODE_2_7) {
                minValence = 2;
                maxValence = 7;
            } else {
                return Status.ioError("Unsupported mode: " + mode);
            }
        } else {
            minValence = 2;
            maxValence = 7;
        }

        if(numVertices < 0) {
            return Status.dracoError("Invalid number of vertices");
        }
        // Set the valences of all initial vertices to 0.
        vertexValences.resize(numVertices, 0);

        int numUniqueValences = maxValence - minValence + 1;

        // Decode all symbols for all contexts.
        contextSymbols.clear();
        contextCounters.resize(numUniqueValences);
        for(int i = 0; i < numUniqueValences; ++i) {
            Pointer<UInt> numSymbols = Pointer.newUInt();
            if(outBuffer.decodeVarint(numSymbols).isError(chain)) return chain.get();
            UInt numSymbolsValue = numSymbols.get();
            if(numSymbolsValue.gt(cornerTable.getNumFaces())) {
                return Status.dracoError("Invalid number of symbols");
            }
            if(numSymbolsValue.gt(0)) {
                CppVector<UInt> contextSymbol = new CppVector<>(DataType.uint32());
                contextSymbol.resize(numSymbolsValue.intValue());
                SymbolDecoding.decode(numSymbolsValue, 1, outBuffer, contextSymbol.getPointer());
                contextCounters.set(i, numSymbolsValue.intValue());
                contextSymbols.add(contextSymbol);
            }
        }
        return Status.ok();
    }

    @Override
    public EdgebreakerTopology decodeSymbol() {
        // First check if we have a valid context.
        if(activeContext != -1) {
            contextCounters.set(activeContext, val -> val - 1);
            int contextCounter = contextCounters.get(activeContext);
            if(contextCounter < 0) return EdgebreakerTopology.INVALID;

            int symbolId = contextSymbols.get(activeContext).get(contextCounter).intValue();
            if(symbolId > 4) return EdgebreakerTopology.INVALID;

            lastSymbol = EdgebreakerTopology.fromSymbol(symbolId);
        } else if (this.getBitstreamVersion() < DracoVersions.getBitstreamVersion(2, 2)) {
            // We don't have a predicted symbol or the symbol was mis-predicted.
            // Decode it directly.
            lastSymbol = super.decodeSymbol();
        } else {
            // The first symbol must be E.
            lastSymbol = EdgebreakerTopology.E;
        }
        return lastSymbol;
    }

    @Override
    public void newActiveCornerReached(CornerIndex corner) {
        CornerIndex next = cornerTable.next(corner);
        CornerIndex prev = cornerTable.previous(corner);
        // Update valences.
        switch(lastSymbol) {
            case C:
            case S:
                vertexValences.set(cornerTable.getVertex(next), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 1);
                break;
            case R:
                vertexValences.set(cornerTable.getVertex(corner), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 2);
                break;
            case L:
                vertexValences.set(cornerTable.getVertex(corner), val -> val + 1);
                vertexValences.set(cornerTable.getVertex(next), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 1);
                break;
            case E:
                vertexValences.set(cornerTable.getVertex(corner), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(next), val -> val + 2);
                vertexValences.set(cornerTable.getVertex(prev), val -> val + 2);
                break;
            default:
                break;
        }
        // Compute the new context that is going to be used to decode the next symbol.
        int activeValence = vertexValences.get(cornerTable.getVertex(next));
        int clampedValence = Math.min(Math.max(activeValence, minValence), maxValence);
        activeContext = clampedValence - minValence;
    }

    @Override
    public void mergeVertices(VertexIndex dest, VertexIndex source) {
        // Update valences on the merged vertices.
        vertexValences.set(dest, val -> val + vertexValences.get(source));
    }
}
