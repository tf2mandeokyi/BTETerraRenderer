package com.mndk.bteterrarenderer.draco.compression.entropy;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.draco.core.DataBuffer;

/**
 * Class for performing rANS encoding using a desired number of precision bits.
 * The max number of precision bits is currently 19. The actual number of
 * symbols in the input alphabet should be (much) smaller than that, otherwise
 * the compression rate may suffer.
 */
public class RAnsEncoder {

    private final int ransPrecision;
    private final int lRansBase;
    private final Ans.Coder ans = new Ans.Coder();

    public RAnsEncoder(int ransPrecisionBits) {
        this.ransPrecision = 1 << ransPrecisionBits;
        this.lRansBase = this.ransPrecision * 4;
    }

    public void writeInit(DataBuffer buf) {
        ans.ansWriteInit(buf, UInt.of(this.lRansBase));
    }

    public ULong writeEnd() {
        if(ans.state.lt(this.lRansBase) || ans.state.ge(this.lRansBase * Ans.DRACO_ANS_IO_BASE)) {
            throw new IllegalStateException("Illegal state number to be serialized");
        }
        UInt state = ans.state.sub(this.lRansBase);
        if(state.lt(1 << 6)) {
            UByte value = state.uByteValue();
            ans.buf.set(ans.bufOffset, value);
            return ULong.of(ans.bufOffset + 1);
        } else if(state.lt(1 << 14)) {
            UInt value = UInt.of(0x01 << 14).add(state);
            ans.buf.memPutLe16(ans.bufOffset, value);
            return ULong.of(ans.bufOffset + 2);
        } else if(state.lt(1 << 22)) {
            UInt value = UInt.of(0x02 << 22).add(state);
            ans.buf.memPutLe24(ans.bufOffset, value);
            return ULong.of(ans.bufOffset + 3);
        } else if(state.lt(1 << 30)) {
            UInt value = UInt.of(0x03 << 30).add(state);
            ans.buf.memPutLe32(ans.bufOffset, value);
            return ULong.of(ans.bufOffset + 4);
        } else {
            throw new IllegalStateException("State is too large to be serialized");
        }
    }

    public void ransWrite(RAnsSymbol sym) {
        UInt p = sym.prob;
        while(ans.state.ge(p.mul(this.lRansBase / this.ransPrecision * Ans.DRACO_ANS_IO_BASE))) {
            UByte value = ans.state.mod(Ans.DRACO_ANS_IO_BASE).uByteValue();
            ans.buf.set(ans.bufOffset++, value);
            ans.state = ans.state.div(Ans.DRACO_ANS_IO_BASE);
        }
        ans.state = ans.state.div(p).mul(this.ransPrecision).add(ans.state.mod(p)).add(sym.cumProb);
    }
}
