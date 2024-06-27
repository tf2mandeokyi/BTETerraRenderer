package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.draco.core.DataBuffer;
import com.mndk.bteterrarenderer.draco.core.Status;
import com.mndk.bteterrarenderer.draco.core.vector.CppVector;

/**
 * Class for performing rANS decoding using a desired number of precision bits.
 * The number of precision bits needs to be the same as with the RAnsEncoder
 * that was used to encode the input data.
 */
public class RAnsDecoder {

    private final int ransPrecision;
    private final int lRansBase;
    private final CppVector<UInt> lutTable = CppVector.create(DataType.uint32());
    private final CppVector<RAnsSymbol> probabilityTable = CppVector.create();
    private final Ans.Decoder ans = new Ans.Decoder();

    public RAnsDecoder(int ransPrecisionBits) {
        this.ransPrecision = 1 << ransPrecisionBits;
        this.lRansBase = this.ransPrecision * 4;
    }

    public Status readInit(DataBuffer buf, long offset) {
        if(offset < 1) return new Status(Status.Code.DRACO_ERROR, "Invalid offset: " + offset);
        ans.buf = buf;
        int x = buf.get(offset - 1).shr(6).intValue();
        if(x == 0) {
            ans.bufOffset = offset - 1;
            ans.state = buf.get(offset - 1).and(0x3F).uIntValue();
        } else if(x == 1) {
            if(offset < 2) return new Status(Status.Code.DRACO_ERROR, "Invalid offset: " + offset);
            ans.bufOffset = offset - 2;
            ans.state = buf.getLE16(offset - 2).and(0x3FFF).uIntValue();
        } else if(x == 2) {
            if(offset < 3) return new Status(Status.Code.DRACO_ERROR, "Invalid offset: " + offset);
            ans.bufOffset = offset - 3;
            ans.state = buf.getLE24(offset - 3).and(0x3FFFFF);
        } else if(x == 3) {
            ans.bufOffset = offset - 4;
            ans.state = buf.getLE32(offset - 4).and(0x3FFFFFFF);
        } else {
            return new Status(Status.Code.DRACO_ERROR, "Invalid x: " + x);
        }
        ans.state = ans.state.add(lRansBase);
        if(ans.state.ge(lRansBase * Ans.DRACO_ANS_IO_BASE)) {
            return new Status(Status.Code.DRACO_ERROR, "state is too large: (state = "
                    + ans.state + ") >= " + lRansBase * Ans.DRACO_ANS_IO_BASE);
        }
        return Status.OK;
    }

    public boolean readEnd() {
        return ans.state.equals(lRansBase);
    }

    public boolean readerHasError() {
        return ans.state.lt(lRansBase) && ans.bufOffset == 0;
    }

    public UInt ransRead() {
        RAnsDecoderSymbol sym = new RAnsDecoderSymbol();
        while(ans.state.lt(lRansBase) && ans.bufOffset > 0) {
            ans.state = ans.state.mul(Ans.DRACO_ANS_IO_BASE).add(ans.buf.get(--ans.bufOffset).uIntValue());
        }
        UInt quo = ans.state.div(ransPrecision);
        UInt rem = ans.state.mod(ransPrecision);
        this.fetchSym(sym, rem);
        ans.state = quo.mul(sym.prob).add(rem).sub(sym.cumProb);
        return sym.val;
    }

    public Status ransBuildLookUpTable(CppVector<UInt> tokenProbs, UInt numSymbols) {
        this.lutTable.resize(ransPrecision);
        this.probabilityTable.resize(numSymbols.intValue(), RAnsSymbol::new);
        UInt cumProb = UInt.ZERO;
        UInt actProb = UInt.ZERO;
        for(UInt i = UInt.ZERO; i.lt(numSymbols); i = i.add(1)) {
            UInt tokenProb = tokenProbs.get(i);
            RAnsSymbol symbol = this.probabilityTable.get(i);
            symbol.prob = tokenProb;
            symbol.cumProb = cumProb;
            cumProb = cumProb.add(tokenProb);
            if(cumProb.gt(ransPrecision)) {
                return new Status(Status.Code.DRACO_ERROR, "cumProb > ransPrecision");
            }
            for(UInt j = actProb; j.lt(cumProb); j = j.add(1)) {
                this.lutTable.set(j, i);
            }
            actProb = cumProb;
        }
        if(!cumProb.equals(ransPrecision)) {
            return new Status(Status.Code.DRACO_ERROR, "cumProb != ransPrecision");
        }
        return Status.OK;
    }

    private void fetchSym(RAnsDecoderSymbol out, UInt rem) {
        UInt symbol = this.lutTable.get(rem);
        out.val = symbol;
        out.prob = this.probabilityTable.get(symbol).prob;
        out.cumProb = this.probabilityTable.get(symbol).cumProb;
    }

}
