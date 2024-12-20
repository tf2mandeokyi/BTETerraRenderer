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

package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BitUtils {

    /** Returns the number of '1' bits within the input 32 bit integer. */
    public int countOneBits32(UInt n) {
        n = n.sub(n.shr(1).and(0x55555555));
        n = n.add(n.shr(2).and(0x33333333)).add(n.and(0x33333333));
        n = n.add(n.shr(4)).and(0x0F0F0F0F).mul(0x01010101).shr(24);
        return n.intValue();
    }

    public UInt reverseBits32(UInt n) {
        n = n.shl(1).and(0x55555555).or(n.and(0x55555555).shr(1));
        n = n.shl(2).and(0x33333333).or(n.and(0x33333333).shr(2));
        n = n.shl(4).and(0x0F0F0F0F).or(n.and(0x0F0F0F0F).shr(4));
        n = n.shl(8).and(0x00FF00FF).or(n.and(0x00FF00FF).shr(8));
        return n.shr(16).or(n.shl(16));
    }

    public void copyBits32(Pointer<UInt> dst, int dstOffset, UInt src, int srcOffset, int nbits) {
        UInt mask = UInt.ZERO.not().shr(32 - nbits).shl(dstOffset);
        dst.set(dst.get().and(mask.not()).or(src.shr(srcOffset).shl(dstOffset).and(mask)));
    }

    public <T> int mostSignificantBit(DataNumberType<T> type, T n) {
        int msb = -1;
        while (!type.equals(n, 0)) {
            msb++;
            n = type.shr(n, 1);
        }
        return msb;
    }

    public void convertSignedIntsToSymbols(Pointer<Integer> inVector, int inValues, Pointer<UInt> outVector) {
        for (int i = 0; i < inValues; i++) {
            outVector.set(i, convertSignedIntToSymbol(DataType.int32(), inVector.get(i), DataType.uint32()));
        }
    }

    public void convertSymbolsToSignedInts(Pointer<UInt> inVector, int inValues, Pointer<Integer> outVector) {
        for (int i = 0; i < inValues; i++) {
            outVector.set(i, convertSymbolToSignedInt(DataType.uint32(), inVector.get(i), DataType.int32()));
        }
    }

    public <T, U> U convertSignedIntToSymbol(DataNumberType<T> signedType, T val, DataNumberType<U> symbolType) {
        if (!signedType.isIntegral()) throw new IllegalArgumentException("T is not integral.");
        if (signedType.ge(val, 0)) {
            return symbolType.shl(symbolType.from(signedType, val), 1);
        }
        val = signedType.negate(signedType.add(val, 1));
        U ret = symbolType.from(signedType, val);
        ret = symbolType.shl(ret, 1);
        ret = symbolType.or(ret, 1);
        return ret;
    }

    public <U, T> T convertSymbolToSignedInt(DataNumberType<U> symbolType, U val, DataNumberType<T> signedType) {
        if (!symbolType.isIntegral()) throw new IllegalArgumentException("T is not integral.");
        boolean isPositive = !DataType.bool().from(symbolType, symbolType.and(val, 1));
        val = symbolType.shr(val, 1);
        T ret = signedType.from(symbolType, val);
        if (isPositive) return ret;
        ret = signedType.sub(signedType.negate(ret), 1);
        return ret;
    }

    private <T> Status decodeVarintUnsigned(long depth, Pointer<T> outVal, DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        DataNumberType<T> outType = outVal.getType().asNumber();
        long maxDepth = outType.byteSize() + 1 + (outType.byteSize() >> 3);
        if (depth > maxDepth) {
            return Status.dracoError("Varint decoding depth exceeded");
        }
        Pointer<UByte> inRef = Pointer.newUByte();
        if (buffer.decode(inRef).isError(chain)) return chain.get();
        UByte in = inRef.get();
        if (!in.and(1 << 7).equals(0)) {
            if (decodeVarintUnsigned(depth + 1, outVal, buffer).isError(chain)) return chain.get();
            T val = outVal.get();
            val = outType.shl(val, 7);
            val = outType.or(val, outType.and(outType.from(in), (1 << 7) - 1));
            outVal.set(val);
        } else {
            outVal.set(outType.from(in));
        }
        return Status.ok();
    }

    <T, U> Status decodeVarint(Pointer<T> outVal, DecoderBuffer buffer) {
        StatusChain chain = new StatusChain();

        DataNumberType<T> outType = outVal.getType().asNumber();
        if (outType.isUnsigned()) {
            return decodeVarintUnsigned(1, outVal, buffer);
        } else {
            // T is a signed value. Decode the symbol and convert to signed.
            DataNumberType<U> unsigned = outType.makeUnsigned();
            Pointer<U> symbolRef = unsigned.newOwned();
            if (decodeVarintUnsigned(1, symbolRef, buffer).isError(chain)) return chain.get();
            U symbol = symbolRef.get();
            T out = convertSymbolToSignedInt(unsigned, symbol, outType);
            outVal.set(out);
        }
        return Status.ok();
    }

    <T, U> Status encodeVarint(DataNumberType<T> inType, T val, EncoderBuffer outBuffer) {
        StatusChain chain = new StatusChain();

        if (inType.isUnsigned()) {
            // Coding of unsigned values.
            // 0-6 bit - data
            // 7 bit - next byte?
            UByte out = UByte.ZERO;
            out = out.or(inType.toUByte(inType.and(val, (1 << 7) - 1)));
            if (inType.ge(val, 1 << 7)) {
                out = out.or(1 << 7);
                if (outBuffer.encode(out).isError(chain)) return chain.get();
                return encodeVarint(inType, inType.shr(val, 7), outBuffer);
            }
            return outBuffer.encode(out);
        } else {
            DataNumberType<U> unsigned = inType.makeUnsigned();
            U symbol = convertSignedIntToSymbol(inType, val, unsigned);
            return encodeVarint(unsigned, symbol, outBuffer);
        }
    }
}
