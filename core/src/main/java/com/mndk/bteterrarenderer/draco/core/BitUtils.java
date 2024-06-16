package com.mndk.bteterrarenderer.draco.core;

import lombok.experimental.UtilityClass;

import java.util.concurrent.atomic.AtomicReference;

@UtilityClass
public class BitUtils {

    public int countOneBits32(int n) {
        n -= ((n >> 1) & 0x55555555);
        n = ((n >> 2) & 0x33333333) + (n & 0x33333333);
        return (((n + (n >> 4)) & 0xF0F0F0F) * 0x1010101) >> 24;
    }

    public int reverseBits32(int n) {
        n = ((n >> 1) & 0x55555555) | ((n & 0x55555555) << 1);
        n = ((n >> 2) & 0x33333333) | ((n & 0x33333333) << 2);
        n = ((n >> 4) & 0x0F0F0F0F) | ((n & 0x0F0F0F0F) << 4);
        n = ((n >> 8) & 0x00FF00FF) | ((n & 0x00FF00FF) << 8);
        return (n >> 16) | (n << 16);
    }

    public void copyBits32(int[] dst, int dstOffset, int src, int srcOffset, int nbits) {
        int mask = (~0) >> (32 - nbits) << dstOffset;
        dst[0] = (dst[0] & (~mask)) | (((src >> srcOffset) << dstOffset) & mask);
    }

    public int mostSignificantBit(int n) {
        int msb = -1;
        while (n != 0) {
            msb++;
            n >>= 1;
        }
        return msb;
    }

    public void convertSignedIntsToSymbols(int[] in, int inValues, long[] out) {
        for (int i = 0; i < inValues; i++) {
            out[i] = convertSignedIntToSymbol(DataType.INT32, in[i], DataType.UINT32);
        }
    }

    public void convertSymbolsToSignedInts(long[] in, int inValues, int[] out) {
        for (int i = 0; i < inValues; i++) {
            out[i] = convertSymbolToSignedInt(DataType.UINT32, in[i], DataType.INT32);
        }
    }

    public <T, U> U convertSignedIntToSymbol(DataType<T> signedType, T val, DataType<U> symbolType) {
        if(!signedType.isIntegral()) throw new IllegalArgumentException("T is not integral.");
        if(signedType.ge(val, signedType.staticCast(0))) {
            return symbolType.staticCast(signedType.shiftLeft(val, 1));
        }
        val = signedType.negate(signedType.add(val, signedType.staticCast(1)));
        U ret = symbolType.staticCast(val);
        ret = symbolType.shiftLeft(ret, 1);
        ret = symbolType.or(ret, symbolType.staticCast(1));
        return ret;
    }

    public <U, T> T convertSymbolToSignedInt(DataType<U> symbolType, U val, DataType<T> signedType) {
        if(!symbolType.isIntegral()) throw new IllegalArgumentException("T is not integral.");
        boolean isPositive = !DataType.BOOL.staticCast(symbolType.and(val, symbolType.staticCast(1)));
        val = symbolType.shiftArithRight(val, 1);
        if(isPositive) {
            return signedType.staticCast(val);
        }
        T ret = signedType.staticCast(val);
        ret = signedType.negate(signedType.add(ret, signedType.staticCast(1)));
        return ret;
    }

    public <T> Status decodeVarintUnsigned(int depth, DataType<T> outType, AtomicReference<T> outValRef, DecoderBuffer buffer) {
        StatusChain chain = Status.newChain();

        int maxDepth = outType.size() + 1 + (outType.size() >> 3);
        if (depth > maxDepth) {
            return new Status(Status.Code.DRACO_ERROR, "Varint decoding depth exceeded");
        }
        AtomicReference<Short> inRef = new AtomicReference<>();
        if(buffer.decode(DataType.UINT8, inRef).isError(chain)) return chain.get();
        short in = inRef.get();
        if((in & (1 << 7)) != 0) {
            if(decodeVarintUnsigned(depth + 1, outType, outValRef, buffer).isError(chain)) return chain.get();
            T outVal = outType.shiftLeft(outValRef.get(), 7);
            outVal = outType.or(outVal, outType.and(outType.staticCast(in), outType.staticCast((1 << 7) - 1)));
            outValRef.set(outVal);
        } else {
            outValRef.set(outType.staticCast(in));
        }
        return Status.OK;
    }

    public <T, U> Status decodeVarint(DataType<T> outType, AtomicReference<T> outVal, DecoderBuffer buffer) {
        StatusChain chain = Status.newChain();

        if (outType.isUnsigned()) {
            return decodeVarintUnsigned(1, outType, outVal, buffer);
        } else {
            // T is a signed value. Decode the symbol and convert to signed.
            DataType<U> unsigned = outType.getUnsignedType();
            AtomicReference<U> symbolRef = new AtomicReference<>(unsigned.staticCast(0));
            if (decodeVarintUnsigned(1, unsigned, symbolRef, buffer).isError(chain)) return chain.get();
            outVal.set(convertSymbolToSignedInt(unsigned, symbolRef.get(), outType));
        }
        return Status.OK;
    }

    public <T, U> Status encodeVarint(DataType<T> inType, T val, EncoderBuffer outBuffer) {
        StatusChain chain = Status.newChain();

        if (inType.isUnsigned()) {
            // Coding of unsigned values.
            // 0-6 bit - data
            // 7 bit - next byte?
            short out = 0;
            T geMask = inType.staticCast(1 << 7);
            T orMask = inType.staticCast((1 << 7) - 1);
            out |= DataType.UINT8.staticCast(inType.and(val, orMask));
            if(inType.ge(val, geMask)) {
                out |= 1 << 7;
                if (outBuffer.encode(DataType.UINT8, out).isError(chain)) return chain.get();
                return encodeVarint(inType, inType.shiftArithRight(val, 7), outBuffer);
            }
            return outBuffer.encode(DataType.UINT8, out);
        } else {
            DataType<U> unsigned = inType.getUnsignedType();
            U symbol = convertSignedIntToSymbol(inType, val, unsigned);
            return encodeVarint(unsigned, symbol, outBuffer);
        }
    }
}
