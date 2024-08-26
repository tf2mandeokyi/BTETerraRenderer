package com.mndk.bteterrarenderer.draco.compression.bitcoder;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;
import com.mndk.bteterrarenderer.draco.compression.entropy.Ans;
import com.mndk.bteterrarenderer.draco.core.BitUtils;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import lombok.Getter;
import lombok.Setter;

public class RAnsBitEncoder {

    private final CppVector<ULong> bitCounts = new CppVector<>(DataType.uint64());
    private final CppVector<UInt> bits = new CppVector<>(DataType.uint32());
    @Getter @Setter
    private UInt localBits = UInt.ZERO;
    private int numLocalBits = 0;

    public void startEncoding() {
        this.clear();
    }

    public void encodeBit(boolean bit) {
        if(bit) {
            bitCounts.set(1, val -> val.add(1));
            localBits = localBits.or(1 << numLocalBits);
        } else {
            bitCounts.set(0, val -> val.add(1));
        }
        numLocalBits++;
        if(numLocalBits == 32) {
            bits.pushBack(localBits);
            numLocalBits = 0;
            localBits = UInt.ZERO;
        }
    }

    public void encodeLeastSignificantBits32(int nBits, UInt value) {
        if(nBits > 32 || nBits <= 0) {
            throw new IllegalArgumentException("nBits must be > 0 and <= 32");
        }

        UInt reversed = BitUtils.reverseBits32(value).shr(32 - nBits);
        int ones = BitUtils.countOneBits32(reversed);
        bitCounts.set(0, val -> val.add(nBits - ones));
        bitCounts.set(1, val -> val.add(ones));

        int remaining = 32 - numLocalBits;

        if(nBits <= remaining) {
            Pointer<UInt> localBitsRef = Pointer.newUInt();
            BitUtils.copyBits32(localBitsRef, numLocalBits, reversed, 0, nBits);
            localBits = localBitsRef.get();
            numLocalBits += nBits;
            if(numLocalBits == 32) {
                bits.pushBack(localBits);
                localBits = UInt.ZERO;
                numLocalBits = 0;
            }
        } else {
            Pointer<UInt> localBitsRef = Pointer.newUInt();
            BitUtils.copyBits32(localBitsRef, numLocalBits, reversed, 0, remaining);
            bits.pushBack(localBitsRef.get());
            localBits = UInt.ZERO;
            BitUtils.copyBits32(localBitsRef, 0, reversed, remaining, nBits - remaining);
            localBits = localBitsRef.get();
            numLocalBits = nBits - remaining;
        }
    }

    public void endEncoding(EncoderBuffer targetBuffer) {
        ULong total = bitCounts.get(1).add(bitCounts.get(0));
        if(total.equals(0)) total = ULong.of(1);

        UInt zeroProbRaw = UInt.of((int) (bitCounts.get(0).doubleValue() / total.doubleValue() * 256.0 + 0.5));

        UByte zeroProb = UByte.of(255);
        if(zeroProbRaw.intValue() < 255) {
            zeroProb = zeroProbRaw.uByteValue();
        }

        zeroProb = zeroProb.add(zeroProb.equals(0) ? 1 : 0);

        // Space for 32 bit integer and some extra space.
        CppVector<UByte> buffer = new CppVector<>(DataType.uint8(), (bits.size() + 8) * 8L);
        Ans.Coder ansCoder = new Ans.Coder();
        ansCoder.ansWriteInit(buffer.getRawPointer());

        for(int i = numLocalBits - 1; i >= 0; --i) {
            UByte bit = localBits.shr(i).and(1).uByteValue();
            ansCoder.rabsWrite(bit.intValue(), zeroProb);
        }
        for(long i = bits.size() - 1; i >= 0; --i) {
            UInt bits = this.bits.get(i);
            for(int j = 31; j >= 0; --j) {
                UByte bit = bits.shr(j).and(1).uByteValue();
                ansCoder.rabsWrite(bit.intValue(), zeroProb);
            }
        }

        long sizeInBytes = ansCoder.ansWriteEnd();
        targetBuffer.encode(zeroProb);
        targetBuffer.encodeVarint(DataType.uint32(), UInt.of(sizeInBytes));
        targetBuffer.encode(buffer.getPointer(), sizeInBytes);

        this.clear();
    }

    private void clear() {
        bitCounts.assign(2, ULong.ZERO);
        bits.clear();
        localBits = UInt.ZERO;
        numLocalBits = 0;
    }
}
