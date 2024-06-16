package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class is a wrapper around input data used by MeshDecoder. It provides a
 * basic interface for decoding either typed or variable-bit sized data.
 */
public class DecoderBuffer {

    private DataBuffer data;
    private int dataSize;
    private int pos;
    private final BitDecoder bitDecoder = new BitDecoder();
    @Getter
    private boolean bitMode;
    @Setter
    private int bitstreamVersion;

    public DecoderBuffer() {}
    public DecoderBuffer(DecoderBuffer buffer) {
        this.data = buffer.data;
        this.dataSize = buffer.dataSize;
        this.pos = buffer.pos;
        this.bitMode = buffer.bitMode;
        this.bitstreamVersion = buffer.bitstreamVersion;
    }

    /**
     * Sets the buffer's internal data. Note that no copy of the input data is
     * made so the data owner needs to keep the data valid and unchanged for
     * runtime of the decoder.
     */
    public void init(DataBuffer data, int dataSize) {
        this.init(data, dataSize, bitstreamVersion);
    }
    public void init(DataBuffer data) {
        this.init(data, data.size());
    }

    /** Sets the buffer's internal data. {@code version} is the Draco bitstream version. */
    public void init(DataBuffer data, int dataSize, int version) {
        this.data = data;
        this.dataSize = dataSize;
        this.bitstreamVersion = version;
        this.pos = 0;
    }

    /**
     * Starts decoding a bit sequence.
     * {@code decodeSize} must be true if the size of the encoded bit data was included,
     * during encoding. The size is then returned to {@code outSize}.
     * Returns either {@link Status#OK} or error type.
     */
    public Status startBitDecoding(boolean decodeSize, Consumer<Long> outSize) {
        StatusChain chain = Status.newChain();

        if(decodeSize) {
            if(bitstreamVersion < DracoVersions.getBitstreamVersion(2, 2)) {
                if(decode(DataType.INT64, outSize).isError(chain)) return chain.get();
            } else {
                AtomicReference<Long> sizeRef = new AtomicReference<>(0L);
                if(BitUtils.decodeVarint(DataType.INT64, sizeRef, this).isError(chain)) return chain.get();
                outSize.accept(sizeRef.get());
            }
        }
        bitMode = true;
        bitDecoder.reset(data, pos, dataSize - pos);
        return Status.OK;
    }

    /**
     * Ends the decoding of the bit sequence and return to the default
     * byte-aligned decoding.
     */
    public void endBitDecoding() {
        bitMode = false;
        int bitsDecoded = bitDecoder.bitsDecoded();
        int bytesDecoded = (bitsDecoded + 7) / 8;
        pos += bytesDecoded;
    }

    /**
     * Decodes up to 32 bits into {@code outValue}. Can be called only in between
     * {@link DecoderBuffer#startBitDecoding} and {@link DecoderBuffer#endBitDecoding}.
     * Returns {@code null} on error.
     */
    @Nullable
    public Status decodeLeastSignificantBits32(int nBits, Consumer<Long> outValue) {
        if(!bitMode) return null;
        return bitDecoder.getBits(nBits, outValue);
    }

    /**
     * Decodes an arbitrary data type.
     * Can be used only when we are not decoding a bit-sequence.
     * Returns {@code false} on error.
     */
    public <T> Status decode(DataType<T> outType, Consumer<T> outVal) {
        StatusChain chain = Status.newChain();
        if(peek(outType, outVal).isError(chain)) return chain.get();
        pos += outType.size();
        return Status.OK;
    }
    @Nullable
    public <T> T decode(DataType<T> outType) {
        AtomicReference<T> outVal = new AtomicReference<>();
        return decode(outType, outVal).isError(null) ? null : outVal.get();
    }
    public <T> Status decode(DataType<T> outType, AtomicReference<T> outVal) {
        return decode(outType, outVal::set);
    }
    public <T> Status decode(DataType<T> outType, BiConsumer<Integer, T> outData, int size) {
        StatusChain chain = Status.newChain();
        if(peek(outType, outData, size).isError(chain)) return chain.get();
        pos += outType.size() * size;
        return Status.OK;
    }

    /** Decodes an arbitrary data, but does not advance the reading position. */
    public <T> Status peek(DataType<T> outType, Consumer<T> outVal) {
        if(dataSize < pos + outType.size()) {
            return new Status(Status.Code.IO_ERROR, "Buffer overflow");
        }
        outVal.accept(outType.getBuf(data, pos));
        return Status.OK;
    }
    @Nullable
    public <T> T peek(DataType<T> outType) {
        AtomicReference<T> outVal = new AtomicReference<>();
        return peek(outType, outVal).isError(null) ? null : outVal.get();
    }
    public <T> Status peek(DataType<T> outType, AtomicReference<T> outVal) {
        return peek(outType, outVal::set);
    }
    public <T> Status peek(DataType<T> outType, BiConsumer<Integer, T> outData, int size) {
        if(dataSize < pos + outType.size() * size) {
            return new Status(Status.Code.IO_ERROR, "Buffer overflow");
        }
        for(int i = 0; i < size; i++) {
            outData.accept(i, outType.getBuf(data, pos + outType.size() * i));
        }
        return Status.OK;
    }

    /** Discards {@code bytes} from the input buffer. */
    public void advance(int bytes) {
        pos += bytes;
    }

    /**
     * Moves the parsing position to a specific offset from the beginning of the
     * input data.
     */
    public void startDecodingFrom(int offset) {
        pos = offset;
    }

    public int getRemainingSize() {
        return dataSize - pos;
    }

    public int getDecodedSize() {
        return pos;
    }

    public static class BitDecoder {

        private DataBuffer bitBuffer;
        private int bitOffset;
        private int size;

        /** Sets the bit buffer to {@code byteBuffer}. */
        public void reset(DataBuffer b, int offset, int s) {
            this.bitOffset = offset;
            this.bitBuffer = b;
            this.size = s;
        }

        /** Returns number of bits decoded so far. */
        public int bitsDecoded() {
            return bitOffset;
        }

        /** Returns number of bits available for decoding. */
        public int availBits() {
            return (size * 8) - bitOffset;
        }

        public int ensureBits(int k) {
            if(k > 24) {
                throw new IllegalArgumentException("k must be less than or equal to 24");
            }
            if(k > availBits()) {
                throw new IllegalArgumentException("Not enough bits available");
            }
            int buf = 0;
            for(int i = 0; i < k; i++) {
                buf |= peekBit(i) << i;
            }
            return buf;
        }

        public void consumeBits(int k) {
            bitOffset += k;
        }

        /** Returns {@code nBits} bits in {@code x} or {@code null} if fails. */
        public Status getBits(int nBits, Consumer<Long> x) {
            if(nBits > 32) {
                return new Status(Status.Code.IO_ERROR, "nBits must be less than or equal to 32");
            }
            long value = 0;
            for(int bit = 0; bit < nBits; bit++) {
                value |= (long) getBit() << bit;
            }
            x.accept(value);
            return Status.OK;
        }

        private int getBit() {
            int off = bitOffset;
            int byteOffset = off >> 3;
            int bitShift = off & 0x7;
            if(byteOffset < size) {
                int bit = (bitBuffer.get(byteOffset) >>> bitShift) & 1;
                bitOffset = off + 1;
                return bit;
            }
            return 0;
        }

        private int peekBit(int offset) {
            int off = bitOffset + offset;
            int byteOffset = off >> 3;
            int bitShift = off & 0x7;
            if(byteOffset < size) {
                return (bitBuffer.get(byteOffset) >>> bitShift) & 1;
            }
            return 0;
        }
    }
}
