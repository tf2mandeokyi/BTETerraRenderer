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

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.*;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

/**
 * Class representing a buffer that can be used for either for byte-aligned
 * encoding of arbitrary data structures or for encoding of variable-length
 * bit data.
 */
public class EncoderBuffer {

    private final CppVector<UByte> buffer = new CppVector<>(DataType.uint8());
    private BitEncoder bitEncoder = null;
    private long bitEncoderReservedBytes = 0;
    private boolean encodeBitSequenceSize = false;

    public void clear() {
        buffer.clear();
        bitEncoderReservedBytes = 0;
    }

    public void resize(long nBytes) {
        buffer.resize(nBytes);
    }

    /**
     * Start encoding a bit sequence. A maximum size of the sequence needs to
     * be known upfront.
     * If {@code encodeSize} is true, the size of encoded bit sequence is stored before
     * the sequence. Decoder can then use this size to skip over the bit sequence
     * if needed.
     * Returns {@code false} on error.
     */
    public Status startBitEncoding(long requiredBits, boolean encodeSize) {
        if (bitEncoderActive()) {
            return Status.ioError("Bit encoding mode active");
        }
        if (requiredBits <= 0) {
            return Status.invalidParameter("Invalid size");
        }
        encodeBitSequenceSize = encodeSize;
        long requiredBytes = (requiredBits + 7) / 8;
        bitEncoderReservedBytes = requiredBytes;
        long bufferStartSize = buffer.size();
        if (encodeSize) {
            bufferStartSize += DataType.uint64().byteSize();
        }
        buffer.resize(bufferStartSize + requiredBytes);
        RawPointer data = buffer.getRawPointer().rawAdd(bufferStartSize);
        bitEncoder = new BitEncoder(data);
        return Status.ok();
    }

    public void endBitEncoding() {
        if (!bitEncoderActive()) {
            return;
        }
        // Get the number of encoded bits and bytes (rounded up).
        long encodedBits = bitEncoder.bits();
        long encodedBytes = (encodedBits + 7) / 8;
        // Flush all cached bits that are not in the bit encoder's main buffer.
        bitEncoder.flush(0);
        // Encode size if needed.
        if (encodeBitSequenceSize) {
            RawPointer outMem = buffer.getRawPointer().rawAdd(size());
            outMem = outMem.rawAdd(-bitEncoderReservedBytes - DataType.uint64().byteSize());

            EncoderBuffer varSizeBuffer = new EncoderBuffer();
            varSizeBuffer.encodeVarint(ULong.of(encodedBytes));
            long sizeLen = varSizeBuffer.size();
            RawPointer dst = outMem.rawAdd(sizeLen);
            RawPointer src = outMem.rawAdd(DataType.uint64().byteSize());
            PointerHelper.rawCopy(src, dst, encodedBytes);

            // Store the size of the encoded data.
            PointerHelper.rawCopy(varSizeBuffer.getData(), outMem, sizeLen);

            // We need to account for the difference between the preallocated and actual
            // storage needed for storing the encoded length. This will be used later to
            // compute the correct size of buffer.
            bitEncoderReservedBytes += DataType.uint64().byteSize() - sizeLen;
        }
        this.resize(buffer.size() - bitEncoderReservedBytes + encodedBytes);
        bitEncoderReservedBytes = 0;
    }

    public Status encodeLeastSignificantBits32(int nbits, UInt value) {
        if (!bitEncoderActive()) return Status.ioError("Bit encoding mode not active");
        bitEncoder.putBits(value, nbits);
        return Status.ok();
    }

    public <T> Status encodeVarint(DataNumberType<T> inType, T val) {
        return BitUtils.encodeVarint(inType, val, this);
    }
    public <T extends CppNumber<T>> Status encodeVarint(T val) {
        return BitUtils.encodeVarint(val.getType(), val, this);
    }

    public <T> Status encode(DataType<T> inType, T data) {
        if (bitEncoderActive()) return Status.ioError("Bit encoding mode active");
        this.buffer.insert(this.buffer.size(), inType.newOwned(data).asRawToUByte(), inType.byteSize());
        return Status.ok();
    }
    public Status encode(RawPointer inValue, long size) {
        if (bitEncoderActive()) return Status.ioError("Bit encoding mode active");
        this.buffer.insert(this.buffer.size(), inValue.toUByte(), size);
        return Status.ok();
    }
    public <T extends CppNumber<T>> Status encode(T val) {
        return encode(val.getType(), val);
    }
    public <T> Status encode(Pointer<T> data, long size) {
        if (bitEncoderActive()) return Status.ioError("Bit encoding mode active");
        this.buffer.insert(this.buffer.size(), data.asRawToUByte(), data.getType().byteSize() * size);
        return Status.ok();
    }

    public boolean bitEncoderActive() {
        return bitEncoderReservedBytes > 0;
    }

    public RawPointer getData() {
        return buffer.getRawPointer();
    }

    public long size() {
        return buffer.size();
    }

    public static class BitEncoder {

        private final RawPointer bitBuffer;
        private long bitOffset;

        public BitEncoder(RawPointer bitBuffer) {
            this.bitBuffer = bitBuffer;
            this.bitOffset = 0;
        }

        public void putBits(UInt data, int nbits) {
            for (int bit = 0; bit < nbits; ++bit) {
                putBit(data.shr(bit).and(1).uByteValue());
            }
        }

        public long bits() {
            return bitOffset;
        }

        public void flush(int i) {
            // Do nothing
        }

        public static int bitsRequired(UInt x) {
            return BitUtils.mostSignificantBit(DataType.uint32(), x);
        }

        private void putBit(UByte value) {
            final int byteSize = 8;
            final long off = bitOffset;
            final long byteOffset = off / byteSize;
            final int bitShift = (int) (off % byteSize);

            UByte bufferValue = bitBuffer.getRawUByte(byteOffset);
            bufferValue = bufferValue.and(UByte.of(~(1 << bitShift)));
            bufferValue = bufferValue.or(value.shl(bitShift));
            bitBuffer.setRawByte(byteOffset, bufferValue);
            bitOffset++;
        }
    }

}
