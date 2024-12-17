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

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Class is a wrapper around input data used by MeshDecoder. It provides a
 * basic interface for decoding either typed or variable-bit sized data.
 */
public class DecoderBuffer {

    @Getter
    private RawPointer data;
    private long dataSize;
    @Getter
    private long pos;
    private final BitDecoder bitDecoder = new BitDecoder();
    @Getter
    private boolean bitDecoderActive;
    @Getter @Setter
    private int bitstreamVersion;

    public DecoderBuffer() {}
    public DecoderBuffer(DecoderBuffer buffer) {
        this.reset(buffer);
    }

    /**
     * Sets the buffer's internal data. Note that no copy of the input data is
     * made so the data owner needs to keep the data valid and unchanged for
     * runtime of the decoder.
     */
    public void init(RawPointer data, long dataSize) {
        this.init(data, dataSize, bitstreamVersion);
    }
    public void init(byte[] data) {
        this.init(Pointer.wrap(data).asRaw(), data.length);
    }
    public void init(InputStream inputStream) throws IOException { this.init(IOUtil.readAllBytes(inputStream)); }
    public void init(ByteBuf byteBuf) { this.init(IOUtil.readAllBytes(byteBuf)); }
    public void init(ByteBuffer byteBuffer) { this.init(IOUtil.readAllBytes(byteBuffer)); }

    /** Sets the buffer's internal data. {@code version} is the Draco bitstream version. */
    public void init(RawPointer data, long dataSize, int version) {
        this.data = data;
        this.dataSize = dataSize;
        this.bitstreamVersion = version;
        this.pos = 0;
    }

    public void reset(DecoderBuffer buffer) {
        this.data = buffer.data;
        this.dataSize = buffer.dataSize;
        this.pos = buffer.pos;
        this.bitDecoderActive = buffer.bitDecoderActive;
        this.bitstreamVersion = buffer.bitstreamVersion;
    }

    /**
     * Starts decoding a bit sequence.
     * {@code decodeSize} must be true if the size of the encoded bit data was included,
     * during encoding. The size is then returned to {@code outSize}.
     * Returns either {@link Status#ok()} or error type.
     */
    public Status startBitDecoding(boolean decodeSize, Pointer<ULong> outSize) {
        StatusChain chain = new StatusChain();

        if (decodeSize) {
            if (bitstreamVersion < DracoVersions.getBitstreamVersion(2, 2)) {
                if (this.decode(outSize).isError(chain)) return chain.get();
            } else {
                if (this.decodeVarint(outSize).isError(chain)) return chain.get();
            }
        }
        bitDecoderActive = true;
        bitDecoder.reset(this.getDataHead(), this.getRemainingSize());
        return Status.ok();
    }

    /**
     * Ends the decoding of the bit sequence and return to the default
     * byte-aligned decoding.
     */
    public void endBitDecoding() {
        bitDecoderActive = false;
        long bitsDecoded = bitDecoder.bitsDecoded();
        long bytesDecoded = (bitsDecoded + 7) / 8;
        pos += bytesDecoded;
    }

    /**
     * Decodes up to 32 bits into {@code outValue}. Can be called only in between
     * {@link DecoderBuffer#startBitDecoding} and {@link DecoderBuffer#endBitDecoding}.
     */
    public Status decodeLeastSignificantBits32(UInt nBits, Pointer<UInt> outValue) {
        if (!bitDecoderActive) return Status.ioError("Bit decoding not started");
        return bitDecoder.getBits(nBits.intValue(), outValue);
    }

    /**
     * Decodes up to 32 bits into {@code outValue}. Can be called only in between
     * {@link DecoderBuffer#startBitDecoding} and {@link DecoderBuffer#endBitDecoding}.
     */
    public Status decodeLeastSignificantBits32(int nBits, Pointer<UInt> outValue) {
        if (!bitDecoderActive) return Status.ioError("Bit decoding not started");
        return bitDecoder.getBits(nBits, outValue);
    }

    public <T> Status decodeVarint(Pointer<T> outVal) {
        return BitUtils.decodeVarint(outVal, this);
    }

    /**
     * Decodes an arbitrary data type.
     * Can be used only when we are not decoding a bit-sequence.
     * Returns {@code false} on error.
     */
    public <T> Status decode(Pointer<T> outVal) {
        Status status = peek(outVal);
        if (status.isError()) return status;
        pos += outVal.getType().byteSize();
        return Status.ok();
    }
    @Nullable
    public <T> T decode(DataType<T> outType) {
        Pointer<T> outVal = outType.newOwned();
        return decode(outVal).isError() ? null : outVal.get();
    }
    public <T> Status decode(Pointer<T> outData, long size) {
        Status status = peek(outData, size);
        if (status.isError()) return status;
        pos += outData.getType().byteSize() * size;
        return Status.ok();
    }
    public Status decode(RawPointer outVal, long size) {
        Status status = peek(outVal, size);
        if (status.isError()) return status;
        pos += size;
        return Status.ok();
    }

    /** Decodes an arbitrary data, but does not advance the reading position. */
    public <T> Status peek(Pointer<T> outVal) {
        DataType<T> outType = outVal.getType();
        if (dataSize < pos + outType.byteSize()) {
            return Status.ioError("Buffer overflow");
        }
        PointerHelper.copySingle(this.data.rawAdd(pos), outVal);
        return Status.ok();
    }
    public Status peek(RawPointer outVal, long size) {
        if (dataSize < pos + size) {
            return Status.ioError("Buffer overflow");
        }
        PointerHelper.rawCopy(this.data.rawAdd(pos), outVal, size);
        return Status.ok();
    }
    @Nullable
    public <T> T peek(DataType<T> outType) {
        Pointer<T> outVal = outType.newOwned();
        return peek(outVal).isError() ? null : outVal.get();
    }
    public <T> Status peek(Pointer<T> outVal, long size) {
        DataType<T> outType = outVal.getType();
        long byteSize = outType.byteSize();
        if (dataSize < pos + byteSize * size) {
            return Status.ioError("Buffer overflow");
        }
        PointerHelper.copyMultiple(this.data.rawAdd(pos), outVal, size);
        return Status.ok();
    }

    /** Discards {@code bytes} from the input buffer. */
    public void advance(long bytes) {
        pos += bytes;
    }

    /**
     * Moves the parsing position to a specific offset from the beginning of the
     * input data.
     */
    public void startDecodingFrom(long offset) {
        pos = offset;
    }

    public RawPointer getDataHead() {
        return data.rawAdd(pos);
    }

    public long getRemainingSize() {
        return dataSize - pos;
    }

    public long getDecodedSize() {
        return pos;
    }

    public static class BitDecoder {

        private RawPointer bitBuffer;
        private long bitOffset;
        private long byteSize;

        /** Sets the bit buffer to {@code byteBuffer}. */
        public void reset(RawPointer b, long s) {
            this.bitOffset = 0;
            this.bitBuffer = b;
            this.byteSize = s;
        }

        /** Returns number of bits decoded so far. */
        public long bitsDecoded() {
            return bitOffset;
        }

        /** Returns number of bits available for decoding. */
        public long availBits() {
            return (byteSize * 8) - bitOffset;
        }

        public UInt ensureBits(int k) {
            if (k > 24) {
                throw new IllegalArgumentException("k must be less than or equal to 24");
            }
            if (k > availBits()) {
                throw new IllegalArgumentException("Not enough bits available");
            }
            int buf = 0;
            for (int i = 0; i < k; i++) {
                buf |= peekBit(i) << i;
            }
            return UInt.of(buf);
        }

        public void consumeBits(int k) {
            bitOffset += k;
        }

        /** Returns {@code nBits} bits in {@code x} or {@code null} if fails. */
        public Status getBits(int nBits, Pointer<UInt> x) {
            if (nBits > 32) {
                return Status.ioError("nBits must be less than or equal to 32, instead got " + nBits);
            }
            UInt value = UInt.ZERO;
            for (int bit = 0; bit < nBits; bit++) {
                value = value.or(getBit() << bit);
            }
            x.set(value);
            return Status.ok();
        }

        private int getBit() {
            long off = bitOffset;
            long byteOffset = off >> 3;
            int bitShift = (int) (off & 0x7);
            if (byteOffset < byteSize) {
                int bit = bitBuffer.getRawUByte(byteOffset).shr(bitShift).and(1).intValue();
                bitOffset = off + 1;
                return bit;
            }
            return 0;
        }

        private int peekBit(int offset) {
            long off = bitOffset + offset;
            long byteOffset = off >> 3;
            int bitShift = (int) (off & 0x7);
            if (byteOffset < byteSize) {
                return bitBuffer.getRawUByte(byteOffset).shr(bitShift).and(1).intValue();
            }
            return 0;
        }
    }
}
