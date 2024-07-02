package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataIOManager;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.draco.compression.config.DracoVersions;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
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
    private boolean bitDecoderActive;
    @Getter @Setter
    private int bitstreamVersion;

    public DecoderBuffer() {}
    public DecoderBuffer(DecoderBuffer buffer) {
        this.data = buffer.data;
        this.dataSize = buffer.dataSize;
        this.pos = buffer.pos;
        this.bitDecoderActive = buffer.bitDecoderActive;
        this.bitstreamVersion = buffer.bitstreamVersion;
    }

    public void init(InputStream inputStream) throws IOException {
        this.init(new DataBuffer(inputStream));
    }

    public void init(ByteBuf byteBuf) {
        this.init(new DataBuffer(byteBuf));
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
     * Returns either {@link Status#ok()} or error type.
     */
    public Status startBitDecoding(boolean decodeSize, @Nonnull Consumer<ULong> outSize) {
        StatusChain chain = new StatusChain();

        if(decodeSize) {
            if(bitstreamVersion < DracoVersions.getBitstreamVersion(2, 2)) {
                if(decode(DataType.uint64(), outSize).isError(chain)) return chain.get();
            } else {
                AtomicReference<ULong> sizeRef = new AtomicReference<>();
                if(decodeVarint(DataType.uint64(), sizeRef).isError(chain)) return chain.get();
                outSize.accept(sizeRef.get());
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
    public Status decodeLeastSignificantBits32(UInt nBits, Consumer<UInt> outValue) {
        if(!bitDecoderActive) return Status.ioError("Bit decoding not started");
        return bitDecoder.getBits(nBits.intValue(), outValue);
    }

    /**
     * Decodes up to 32 bits into {@code outValue}. Can be called only in between
     * {@link DecoderBuffer#startBitDecoding} and {@link DecoderBuffer#endBitDecoding}.
     */
    public Status decodeLeastSignificantBits32(int nBits, Consumer<UInt> outValue) {
        if(!bitDecoderActive) return Status.ioError("Bit decoding not started");
        return bitDecoder.getBits(nBits, outValue);
    }

    public <T> Status decodeVarint(DataNumberType<T, ?> outType, AtomicReference<T> outVal) {
        return BitUtils.decodeVarint(outType, outVal, this);
    }

    /**
     * Decodes an arbitrary data type.
     * Can be used only when we are not decoding a bit-sequence.
     * Returns {@code false} on error.
     */
    public <T> Status decode(DataIOManager<T> outType, @Nonnull Consumer<T> outVal) {
        StatusChain chain = new StatusChain();
        if(peek(outType, outVal).isError(chain)) return chain.get();
        pos += outType.size();
        return Status.ok();
    }
    @Nullable
    public <T> T decode(DataIOManager<T> outType) {
        AtomicReference<T> outVal = new AtomicReference<>();
        return decode(outType, outVal::set).isError() ? null : outVal.get();
    }
    public <T> Status decode(DataIOManager<T> outType, BiConsumer<Integer, T> outData, int size) {
        StatusChain chain = new StatusChain();
        if(peek(outType, outData, size).isError(chain)) return chain.get();
        pos += outType.size() * size;
        return Status.ok();
    }
    public <T, TArray> Status decode(DataType<T, TArray> outType, TArray outData, int size) {
        StatusChain chain = new StatusChain();
        if(peek(outType, outData, size).isError(chain)) return chain.get();
        pos += outType.size() * size;
        return Status.ok();
    }

    /** Decodes an arbitrary data, but does not advance the reading position. */
    public <T> Status peek(DataIOManager<T> outType, @Nonnull Consumer<T> outVal) {
        if(dataSize < pos + outType.size()) {
            return Status.ioError("Buffer overflow");
        }
        outVal.accept(this.data.read(outType, pos));
        return Status.ok();
    }
    @Nullable
    public <T> T peek(DataIOManager<T> outType) {
        AtomicReference<T> outVal = new AtomicReference<>();
        return peek(outType, outVal).isError() ? null : outVal.get();
    }
    public <T> Status peek(DataIOManager<T> outType, @Nonnull AtomicReference<T> outVal) {
        return peek(outType, outVal::set);
    }
    public <T, TArray> Status peek(DataType<T, TArray> outType, @Nonnull TArray outData, int size) {
        return peek(outType, outType.setter(outData), size);
    }
    public <T> Status peek(DataIOManager<T> outType, BiConsumer<Integer, T> outData, int size) {
        if(dataSize < pos + outType.size() * size) {
            return Status.ioError("Buffer overflow");
        }
        for(int i = 0; i < size; i++) {
            T value = this.data.read(outType, pos + outType.size() * i);
            outData.accept(i, value);
        }
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

    public DataBuffer getDataHead() {
        return data.withOffset(pos);
    }

    public long getRemainingSize() {
        return dataSize - pos;
    }

    public long getDecodedSize() {
        return pos;
    }

//    public void skipLine() { DracoParserUtils.skipLine(this); }
//    public void skipWhitespace() { DracoParserUtils.skipWhitespace(this); }
//    public boolean peekWhitespace(AtomicBoolean endReached) { return DracoParserUtils.peekWhitespace(this, endReached); }
//    public Status parseFloat(AtomicReference<Float> valRef) { return DracoParserUtils.parseFloat(this, valRef); }
//    public DecoderBuffer parseLine() { return DracoParserUtils.parseLineIntoDecoderBuffer(this); }

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
        public Status getBits(int nBits, Consumer<UInt> x) {
            if(nBits > 32) {
                return Status.ioError("nBits must be less than or equal to 32, instead got " + nBits);
            }
            UInt value = UInt.ZERO;
            for(int bit = 0; bit < nBits; bit++) {
                value = value.or(getBit() << bit);
            }
            x.accept(value);
            return Status.ok();
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
