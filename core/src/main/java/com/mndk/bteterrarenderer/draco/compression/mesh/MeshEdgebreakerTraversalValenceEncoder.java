package com.mndk.bteterrarenderer.draco.compression.mesh;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.attributes.CornerIndex;
import com.mndk.bteterrarenderer.draco.attributes.VertexIndex;
import com.mndk.bteterrarenderer.draco.compression.entropy.SymbolEncoding;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.StatusChain;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.IndexTypeVector;
import com.mndk.bteterrarenderer.draco.mesh.CornerTable;

import java.util.ArrayList;
import java.util.List;

public class MeshEdgebreakerTraversalValenceEncoder extends MeshEdgebreakerTraversalEncoder {

    private CornerTable cornerTable;
    /* Explicit map between corners and vertices. */
    private final IndexTypeVector<CornerIndex, VertexIndex> cornerToVertexMap =
            new IndexTypeVector<>(VertexIndex.type());
    private final IndexTypeVector<VertexIndex, Integer> vertexValences =
            new IndexTypeVector<>(DataType.int32());
    /* Previously encoded symbol. */
    private EdgebreakerTopology prevSymbol = EdgebreakerTopology.INVALID;
    private CornerIndex lastCorner = CornerIndex.INVALID;
    /* Explicitly count the number of encoded symbols. */
    private int numSymbols = 0;

    private int minValence = 2;
    private int maxValence = 7;
    private final List<CppVector<UInt>> contextSymbols = new ArrayList<>();

    @Override
    public Status init(MeshEdgebreakerEncoderImplInterface encoder) {
        StatusChain chain = new StatusChain();
        if(super.init(encoder).isError(chain)) return chain.get();
        minValence = 2;
        maxValence = 7;
        cornerTable = encoder.getCornerTable();

        // Initialize valences of all vertices.
        vertexValences.resize(cornerTable.getNumVertices());
        for(VertexIndex i : VertexIndex.range(0, (int) vertexValences.size())) {
            vertexValences.set(i, cornerTable.getValence(i));
        }

        // Replicate the corner to vertex map from the corner table.
        cornerToVertexMap.resize(cornerTable.getNumCorners());
        for(CornerIndex i : CornerIndex.range(0, cornerTable.getNumCorners())) {
            cornerToVertexMap.set(i, cornerTable.getVertex(i));
        }
        int numUniqueValences = maxValence - minValence + 1;

        contextSymbols.clear();
        for(int i = 0; i < numUniqueValences; ++i) {
            contextSymbols.add(new CppVector<>(DataType.uint32()));
        }
        return Status.ok();
    }

    @Override
    public void newCornerReached(CornerIndex corner) {
        lastCorner = corner;
    }

    @Override
    public void encodeSymbol(EdgebreakerTopology symbol) {
        numSymbols++;
        // Update valences on the mesh and compute the context that is going to be
        // used to encode the processed symbol.

        CornerIndex next = cornerTable.next(lastCorner);
        CornerIndex prev = cornerTable.previous(lastCorner);

        // Get valence on the tip corner of the active edge.
        int activeValence = vertexValences.get(cornerToVertexMap.get(next));
        switch(symbol) {
            case C: // Compute prediction.
            case S:
                // Update valences.
                vertexValences.set(cornerToVertexMap.get(next), val -> val - 1);
                vertexValences.set(cornerToVertexMap.get(prev), val -> val - 1);
                if(symbol == EdgebreakerTopology.S) {
                    // Count the number of faces on the left side of the split vertex and
                    // update the valence on the "left vertex".
                    int numLeftFaces = 0;
                    CornerIndex actC = cornerTable.opposite(prev);
                    while(actC.isValid()) {
                        if(this.getEncoderImpl().isFaceEncoded(cornerTable.getFace(actC).getValue())) {
                            break; // Stop when we reach the first visited face.
                        }
                        numLeftFaces++;
                        actC = cornerTable.opposite(cornerTable.next(actC));
                    }
                    vertexValences.set(cornerToVertexMap.get(lastCorner), numLeftFaces + 1);

                    // Create a new vertex for the right side and count the number of
                    // faces that should be attached to this vertex.
                    int newVertId = (int) vertexValences.size();
                    int numRightFaces = 0;

                    actC = cornerTable.opposite(next);
                    while(actC.isValid()) {
                        if(this.getEncoderImpl().isFaceEncoded(cornerTable.getFace(actC).getValue())) {
                            break; // Stop when we reach the first visited face.
                        }
                        numRightFaces++;
                        // Map corners on the right side to the newly created vertex.
                        cornerToVertexMap.set(cornerTable.next(actC), VertexIndex.of(newVertId));
                        actC = cornerTable.opposite(cornerTable.previous(actC));
                    }
                    vertexValences.pushBack(numRightFaces + 1);
                }
                break;
            case R:
                // Update valences.
                vertexValences.set(cornerToVertexMap.get(lastCorner), val -> val - 1);
                vertexValences.set(cornerToVertexMap.get(next), val -> val - 1);
                vertexValences.set(cornerToVertexMap.get(prev), val -> val - 2);
                break;
            case L:
                vertexValences.set(cornerToVertexMap.get(lastCorner), val -> val - 1);
                vertexValences.set(cornerToVertexMap.get(next), val -> val - 2);
                vertexValences.set(cornerToVertexMap.get(prev), val -> val - 1);
                break;
            case E:
                vertexValences.set(cornerToVertexMap.get(lastCorner), val -> val - 2);
                vertexValences.set(cornerToVertexMap.get(next), val -> val - 2);
                vertexValences.set(cornerToVertexMap.get(prev), val -> val - 2);
                break;
            default:
                break;
        }

        if(prevSymbol != EdgebreakerTopology.INVALID) {
            int clampedValence = Math.min(Math.max(activeValence, minValence), maxValence);
            int context = clampedValence - minValence;
            contextSymbols.get(context).pushBack(UInt.of(prevSymbol.getBitPattern()));
        }

        prevSymbol = symbol;
    }

    @Override
    public void done() {
        // Store the init face configurations and attribute seam data
        super.encodeStartFaces();
        super.encodeAttributeSeams();

        // Store the contexts.
        for(CppVector<UInt> context : contextSymbols) {
            this.getOutputBuffer().encodeVarint(DataType.uint32(), UInt.of(context.size()));
            if (!context.isEmpty()) {
                SymbolEncoding.encode(context.getPointer(), (int) context.size(),
                        1, null, this.getOutputBuffer());
            }
        }
    }

    @Override
    public int getNumEncodedSymbols() {
        return numSymbols;
    }
}
