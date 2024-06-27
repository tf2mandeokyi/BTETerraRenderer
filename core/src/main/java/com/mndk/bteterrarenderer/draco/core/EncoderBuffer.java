package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;

import java.util.function.Function;

/**
 * Class representing a buffer that can be used for either for byte-aligned
 * encoding of arbitrary data structures or for encoding of variable-length
 * bit data.
 */
public class EncoderBuffer {

    private DataBuffer buffer = new DataBuffer();
    private BitEncoder bitEncoder = null;
    private long bitEncoderReservedBytes = 0;
    private boolean encodeBitSequenceSize = false;

    public void clear() {
        buffer = new DataBuffer();
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
        if(bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode active");
        }
        if(requiredBits <= 0) {
            return new Status(Status.Code.INVALID_PARAMETER, "Invalid size");
        }
        encodeBitSequenceSize = encodeSize;
        long requiredBytes = (requiredBits + 7) / 8;
        bitEncoderReservedBytes = requiredBytes;
        long bufferStartSize = buffer.size();
        if(encodeSize) {
            bufferStartSize += DataType.uint64().size();
        }
        buffer.resize(bufferStartSize + requiredBytes);
        DataBuffer data = buffer.withOffset(bufferStartSize);
        bitEncoder = new BitEncoder(data);
        return Status.OK;
    }

    public void endBitEncoding() {
        if(!bitEncoderActive()) {
            return;
        }
        // Get the number of encoded bits and bytes (rounded up).
        long encodedBits = bitEncoder.bits();
        long encodedBytes = (encodedBits + 7) / 8;
        // Flush all cached bits that are not in the bit encoder's main buffer.
        bitEncoder.flush(0);
        // Encode size if needed.
        if(encodeBitSequenceSize) {
            long outMem = buffer.size();
            outMem -= bitEncoderReservedBytes + DataType.uint64().size();

            EncoderBuffer varSizeBuffer = new EncoderBuffer();
            BitUtils.encodeVarint(DataType.uint64(), ULong.of(encodedBytes), varSizeBuffer);
            long sizeLen = varSizeBuffer.size();
            long dst = outMem + sizeLen;
            long src = outMem + DataType.uint64().size();
            this.buffer.copyFrom(dst, this.buffer, src, encodedBytes);

            // Store the size of the encoded data.
            this.buffer.copyFrom(outMem, varSizeBuffer.buffer, 0, sizeLen);

            // We need to account for the difference between the preallocated and actual
            // storage needed for storing the encoded length. This will be used later to
            // compute the correct size of buffer.
            bitEncoderReservedBytes += DataType.uint64().size() - sizeLen;
        }
        this.resize(buffer.size() - bitEncoderReservedBytes + encodedBytes);
        bitEncoderReservedBytes = 0;
    }

    public Status encodeLeastSignificantBits32(int nbits, UInt value) {
        if(!bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode not active");
        }
        bitEncoder.putBits(value, nbits);
        return Status.OK;
    }

    public <T> Status encode(DataType<T, ?> inType, T data) {
        if(bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode active");
        }
        long oldSize = this.buffer.size();
        this.buffer.resize(oldSize + inType.size());
        this.buffer.write(inType, oldSize, data);
        return Status.OK;
    }
    public <T, TArray> Status encode(DataType<T, TArray> inType, TArray data, long size) {
        return encode(inType, inType.getter(data), size);
    }
    public <T> Status encode(DataType<T, ?> inType, Function<Integer, T> data, long size) {
        if(bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode active");
        }
        long oldSize = this.buffer.size();
        this.buffer.resize(oldSize + inType.size() * size);
        for(int i = 0; i < size; i++) {
            this.buffer.write(inType, oldSize + i * inType.size(), data.apply(i));
        }
        return Status.OK;
    }

    public boolean bitEncoderActive() {
        return bitEncoderReservedBytes > 0;
    }

    public DataBuffer getData() {
        return buffer;
    }

    public long size() {
        return buffer.size();
    }

    public static class BitEncoder {

        private final DataBuffer bitBuffer;
        private long bitOffset;

        public BitEncoder(DataBuffer bitBuffer) {
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
            return BitUtils.mostSignificantBit(x);
        }

        private void putBit(UByte value) {
            final int byteSize = 8;
            final long off = bitOffset;
            final long byteOffset = off / byteSize;
            final int bitShift = (int) (off % byteSize);

            UByte bufferValue = bitBuffer.get(byteOffset);
            bufferValue = bufferValue.and(UByte.of(~(1 << bitShift)));
            bufferValue = bufferValue.or(value.shl(bitShift));
            bitBuffer.set(byteOffset, bufferValue);
            bitOffset++;
        }
    }

}
