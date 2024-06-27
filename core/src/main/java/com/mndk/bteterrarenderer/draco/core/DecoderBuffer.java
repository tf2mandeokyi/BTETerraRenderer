package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataIOManager;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class is a wrapper around input data used by MeshDecoder. It provides a
 * basic interface for decoding either typed or variable-bit sized data.
 */
public class DecoderBuffer {

    @Getter
    private DataBuffer data;
    private long dataSize;
    @Getter
    private long pos;
    private final BitDecoder bitDecoder = new BitDecoder();
    @Getter
    private boolean bitMode;
    @Getter @Setter
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
    public void init(DataBuffer data, long dataSize) {
        this.init(data, dataSize, bitstreamVersion);
    }
    public void init(DataBuffer data) {
        this.init(data, data.size());
    }

    /** Sets the buffer's internal data. {@code version} is the Draco bitstream version. */
    public void init(DataBuffer data, long dataSize, int version) {
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
    public Status startBitDecoding(boolean decodeSize, @Nonnull Consumer<ULong> outSize) {
        StatusChain chain = Status.newChain();

        if(decodeSize) {
            if(bitstreamVersion < DracoVersions.getBitstreamVersion(2, 2)) {
                if(decode(DataType.uint64(), outSize).isError(chain)) return chain.get();
            } else {
                AtomicReference<ULong> sizeRef = new AtomicReference<>();
                if(BitUtils.decodeVarint(DataType.uint64(), sizeRef, this).isError(chain)) return chain.get();
                outSize.accept(sizeRef.get());
            }
        }
        bitMode = true;
        bitDecoder.reset(this.getDataHead(), this.getRemainingSize());
        return Status.OK;
    }

    /**
     * Ends the decoding of the bit sequence and return to the default
     * byte-aligned decoding.
     */
    public void endBitDecoding() {
        bitMode = false;
        long bitsDecoded = bitDecoder.bitsDecoded();
        long bytesDecoded = (bitsDecoded + 7) / 8;
        pos += bytesDecoded;
    }

    /**
     * Decodes up to 32 bits into {@code outValue}. Can be called only in between
     * {@link DecoderBuffer#startBitDecoding} and {@link DecoderBuffer#endBitDecoding}.
     */
    public Status decodeLeastSignificantBits32(UInt nBits, Consumer<UInt> outValue) {
        if(!bitMode) {
            return new Status(Status.Code.IO_ERROR, "Bit decoding not started");
        }
        return bitDecoder.getBits(nBits, outValue);
    }

    /**
     * Decodes an arbitrary data type.
     * Can be used only when we are not decoding a bit-sequence.
     * Returns {@code false} on error.
     */
    public <T> Status decode(DataIOManager<T> outType, @Nonnull Consumer<T> outVal) {
        StatusChain chain = Status.newChain();
        if(peek(outType, outVal).isError(chain)) return chain.get();
        pos += outType.size();
        return Status.OK;
    }
    @Nullable
    public <T> T decode(DataIOManager<T> outType) {
        AtomicReference<T> outVal = new AtomicReference<>();
        return decode(outType, outVal::set).isError(null) ? null : outVal.get();
    }
    public <T> Status decode(DataIOManager<T> outType, BiConsumer<Integer, T> outData, int size) {
        StatusChain chain = Status.newChain();
        if(peek(outType, outData, size).isError(chain)) return chain.get();
        pos += outType.size() * size;
        return Status.OK;
    }
    public <T, TArray> Status decode(DataType<T, TArray> outType, TArray outData, int size) {
        StatusChain chain = Status.newChain();
        if(peek(outType, outData, size).isError(chain)) return chain.get();
        pos += outType.size() * size;
        return Status.OK;
    }

    /** Decodes an arbitrary data, but does not advance the reading position. */
    public <T> Status peek(DataIOManager<T> outType, @Nonnull Consumer<T> outVal) {
        if(dataSize < pos + outType.size()) {
            return new Status(Status.Code.IO_ERROR, "Buffer overflow");
        }
        outVal.accept(this.data.read(outType, pos));
        return Status.OK;
    }
    @Nullable
    public <T> T peek(DataIOManager<T> outType) {
        AtomicReference<T> outVal = new AtomicReference<>();
        return peek(outType, outVal).isError(null) ? null : outVal.get();
    }
    public <T> Status peek(DataIOManager<T> outType, @Nonnull AtomicReference<T> outVal) {
        return peek(outType, outVal::set);
    }
    public <T, TArray> Status peek(DataType<T, TArray> outType, @Nonnull TArray outData, int size) {
        return peek(outType, outType.setter(outData), size);
    }
    public <T> Status peek(DataIOManager<T> outType, BiConsumer<Integer, T> outData, int size) {
        if(dataSize < pos + outType.size() * size) {
            return new Status(Status.Code.IO_ERROR, "Buffer overflow");
        }
        for(int i = 0; i < size; i++) {
            T value = this.data.read(outType, pos + outType.size() * i);
            outData.accept(i, value);
        }
        return Status.OK;
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

    public DataBuffer getDataHead() {
        return data.withOffset(pos);
    }

    public long getRemainingSize() {
        return dataSize - pos;
    }

    public long getDecodedSize() {
        return pos;
    }

    public static class BitDecoder {

        private DataBuffer bitBuffer;
        private long bitOffset;
        private long byteSize;

        /** Sets the bit buffer to {@code byteBuffer}. */
        public void reset(DataBuffer b, long s) {
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
            return UInt.of(buf);
        }

        public void consumeBits(int k) {
            bitOffset += k;
        }

        /** Returns {@code nBits} bits in {@code x} or {@code null} if fails. */
        public Status getBits(UInt nBits, Consumer<UInt> x) {
            if(nBits.gt(32)) {
                return new Status(Status.Code.IO_ERROR,
                        "nBits must be less than or equal to 32, instead got " + nBits);
            }
            UInt value = UInt.ZERO;
            for(int bit = 0; bit < nBits.intValue(); bit++) {
                value = value.or(getBit() << bit);
            }
            x.accept(value);
            return Status.OK;
        }

        private int getBit() {
            long off = bitOffset;
            long byteOffset = off >> 3;
            int bitShift = (int) (off & 0x7);
            if(byteOffset < byteSize) {
                int bit = bitBuffer.get(byteOffset).shr(bitShift).and(1).intValue();
                bitOffset = off + 1;
                return bit;
            }
            return 0;
        }

        private int peekBit(int offset) {
            long off = bitOffset + offset;
            long byteOffset = off >> 3;
            int bitShift = (int) (off & 0x7);
            if(byteOffset < byteSize) {
                return bitBuffer.get(byteOffset).shr(bitShift).and(1).intValue();
            }
            return 0;
        }
    }
}
