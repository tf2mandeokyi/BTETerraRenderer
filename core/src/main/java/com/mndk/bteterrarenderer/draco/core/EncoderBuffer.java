package com.mndk.bteterrarenderer.draco.core;

import lombok.Getter;

import java.util.function.Function;

/**
 * Class representing a buffer that can be used for either for byte-aligned
 * encoding of arbitrary data structures or for encoding of variable-length
 * bit data.
 */
public class EncoderBuffer {

    @Getter
    private DataBuffer buffer = new DataBuffer();
    private BitEncoder bitEncoder = null;
    private int bitEncoderReservedBytes = 0;
    private boolean encodeBitSequenceSize = false;

    public void clear() {
        buffer = new DataBuffer();
        bitEncoderReservedBytes = 0;
    }

    public void resize(int nBytes) {
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
    public Status startBitEncoding(int requiredBits, boolean encodeSize) {
        if(bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode active");
        }
        if(requiredBits <= 0) {
            return new Status(Status.Code.INVALID_PARAMETER, "Invalid size");
        }
        encodeBitSequenceSize = encodeSize;
        int requiredBytes = (requiredBits + 7) / 8;
        bitEncoderReservedBytes = requiredBytes;
        int bufferStartSize = buffer.size();
        if(encodeSize) {
            bufferStartSize += DataType.UINT64.size();
        }
        buffer.resize(bufferStartSize + requiredBytes);
        bitEncoder = new BitEncoder(buffer, bufferStartSize);
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
            int outMem = buffer.size();
            outMem -= bitEncoderReservedBytes + DataType.UINT64.size();

            EncoderBuffer varSizeBuffer = new EncoderBuffer();
            BitUtils.encodeVarint(DataType.INT64, encodedBytes, varSizeBuffer);
            int sizeLen = varSizeBuffer.size();
            int dst = outMem + sizeLen;
            int src = outMem + DataType.UINT64.size();
            this.buffer.copy(dst, this.buffer, src, (int) encodedBytes);

            // Store the size of the encoded data.
            this.buffer.copy(outMem, varSizeBuffer.buffer, 0, sizeLen);

            // We need to account for the difference between the preallocated and actual
            // storage needed for storing the encoded length. This will be used later to
            // compute the correct size of buffer.
            bitEncoderReservedBytes += DataType.UINT64.size() - sizeLen;
        }
        this.resize(buffer.size() - bitEncoderReservedBytes + (int) encodedBytes);
        bitEncoderReservedBytes = 0;
    }

    public Status encodeLeastSignificantBits32(int nbits, int value) {
        if(!bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode not active");
        }
        bitEncoder.putBits(value, nbits);
        return Status.OK;
    }

    public <T> Status encode(DataType<T> inType, T data) {
        if(bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode active");
        }
        int oldSize = this.buffer.size();
        this.buffer.resize(oldSize + inType.size());
        inType.setBuf(this.buffer, oldSize, data);
        return Status.OK;
    }
    public <T> Status encode(DataType<T> inType, T[] data, int size) {
        return this.encode(inType, i -> data[i], size);
    }
    public <T> Status encode(DataType<T> inType, Function<Integer, T> data, int size) {
        if(bitEncoderActive()) {
            return new Status(Status.Code.IO_ERROR, "Bit encoding mode active");
        }
        int oldSize = this.buffer.size();
        this.buffer.resize(oldSize + inType.size() * size);
        for(int i = 0; i < size; i++) inType.setBuf(this.buffer, oldSize + i * inType.size(), data.apply(i));
        return Status.OK;
    }

    public boolean bitEncoderActive() {
        return bitEncoderReservedBytes > 0;
    }

    public int size() {
        return buffer.size();
    }

    public static class BitEncoder {

        private final DataBuffer bitBuffer;
        private int bitOffset;

        public BitEncoder(DataBuffer bitBuffer, int offset) {
            this.bitBuffer = bitBuffer;
            this.bitOffset = offset;
        }

        public void putBits(int data, int nbits) {
            for (int bit = 0; bit < nbits; ++bit) {
                putBit((data >>> bit) & 1);
            }
        }

        public int bits() {
            return bitOffset;
        }

        public void flush(int i) {
            // Do nothing
        }

        public static int bitsRequired(int x) {
            return BitUtils.mostSignificantBit(x);
        }

        private void putBit(int value) {
            final int byteSize = 8;
            final long off = bitOffset;
            final long byteOffset = off / byteSize;
            final int bitShift = (int) (off % byteSize);

            byte bufferValue = bitBuffer.get((int) byteOffset);
            bufferValue &= (byte) ~(1 << bitShift);
            bufferValue |= (byte) (value << bitShift);
            bitBuffer.set((int) byteOffset, bufferValue);
            bitOffset++;
        }
    }

}
