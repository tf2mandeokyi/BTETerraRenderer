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

package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.core.Status;

/**
 * Class for performing rANS decoding using a desired number of precision bits.
 * The number of precision bits needs to be the same as with the RAnsEncoder
 * that was used to encode the input data.
 */
public class RAnsDecoder {

    private final int ransPrecision;
    private final int lRansBase;
    private final CppVector<UInt> lutTable = new CppVector<>(DataType.uint32());
    private final CppVector<RAnsSymbol> probabilityTable = new CppVector<>(RAnsSymbol::new);
    private final Ans.Decoder ans = new Ans.Decoder();

    public RAnsDecoder(int ransPrecisionBits) {
        this.ransPrecision = 1 << ransPrecisionBits;
        this.lRansBase = this.ransPrecision * 4;
    }

    public Status readInit(RawPointer buf, long offset) {
        if(offset < 1) return Status.dracoError("Invalid offset: " + offset);
        ans.buf = buf;
        int x = buf.getRawUByte(offset - 1).shr(6).intValue();
        if(x == 0) {
            ans.bufOffset = offset - 1;
            ans.state = buf.getRawUByte(offset - 1).and(0x3F).uIntValue();
        } else if(x == 1) {
            if(offset < 2) return Status.dracoError("Invalid offset: " + offset);
            ans.bufOffset = offset - 2;
            ans.state = Ans.getLE16(buf.rawAdd(offset - 2)).and(0x3FFF).uIntValue();
        } else if(x == 2) {
            if(offset < 3) return Status.dracoError("Invalid offset: " + offset);
            ans.bufOffset = offset - 3;
            ans.state = Ans.getLE24(buf.rawAdd(offset - 3)).and(0x3FFFFF);
        } else if(x == 3) {
            ans.bufOffset = offset - 4;
            ans.state = Ans.getLE32(buf.rawAdd(offset - 4)).and(0x3FFFFFFF);
        } else {
            return Status.dracoError("Invalid x: " + x);
        }
        ans.state = ans.state.add(lRansBase);
        if(ans.state.ge(lRansBase * Ans.DRACO_ANS_IO_BASE)) {
            return Status.dracoError("state is too large: (state = "
                    + ans.state + ") >= " + lRansBase * Ans.DRACO_ANS_IO_BASE);
        }
        return Status.ok();
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
            ans.state = ans.state.mul(Ans.DRACO_ANS_IO_BASE).add(ans.buf.getRawUByte(--ans.bufOffset).uIntValue());
        }
        UInt quo = ans.state.div(ransPrecision);
        UInt rem = ans.state.mod(ransPrecision);
        this.fetchSym(sym, rem);
        ans.state = quo.mul(sym.prob).add(rem).sub(sym.cumProb);
        return sym.val;
    }

    public Status ransBuildLookUpTable(CppVector<UInt> tokenProbs, UInt numSymbols) {
        this.lutTable.resize(ransPrecision);
        this.probabilityTable.resize(numSymbols.intValue());
        UInt cumProb = UInt.ZERO;
        UInt actProb = UInt.ZERO;
        for(UInt i = UInt.ZERO; i.lt(numSymbols); i = i.add(1)) {
            UInt tokenProb = tokenProbs.get(i);
            RAnsSymbol symbol = this.probabilityTable.get(i);
            symbol.prob = tokenProb;
            symbol.cumProb = cumProb;
            cumProb = cumProb.add(tokenProb);
            if(cumProb.gt(ransPrecision)) {
                return Status.dracoError("cumProb > ransPrecision");
            }
            for(UInt j = actProb; j.lt(cumProb); j = j.add(1)) {
                this.lutTable.set(j, i);
            }
            actProb = cumProb;
        }
        if(!cumProb.equals(ransPrecision)) {
            return Status.dracoError("cumProb != ransPrecision");
        }
        return Status.ok();
    }

    private void fetchSym(RAnsDecoderSymbol out, UInt rem) {
        UInt symbol = this.lutTable.get(rem);
        out.val = symbol;
        out.prob = this.probabilityTable.get(symbol).prob;
        out.cumProb = this.probabilityTable.get(symbol).cumProb;
    }

}
