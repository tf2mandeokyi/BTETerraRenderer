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
        return symbols.size();
    }

    public EncoderBuffer getBuffer() {
        return traversalBuffer;
    }

    protected void encodeTraversalSymbols() {
        traversalBuffer.startBitEncoding(encoderImpl.getEncoder().getMesh().getNumFaces() * 3L, true);
        for (int i = symbols.size() - 1; i >= 0; --i) {
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
