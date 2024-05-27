package com.mndk.bteterrarenderer.draco_deprecated.util;

import io.netty.buffer.ByteBuf;

public class BitStreamBuf {
    private final ByteBuf buf;
    private short currentByte;
    private byte bitOffset;

    public BitStreamBuf(ByteBuf buf) {
        this.buf = buf;
        resetBitReader();
    }

    /**
     * Read unsigned n-bit number appearing directly in the bitstream.
     * The bits are read from high to low order.
     * When the bit reading is finished it will always pad the read to the current byte.
     * {@link BitStreamBuf#resetBitReader()} will signify when the bit reading is finished.
     * @return The bit read
     */
    public int readBits(int n) {
        int result = 0;
        for (int i = n - 1; i >= 0; i--) {
            result |= (readBit() << i);
        }

        return result;
    }

    /**
     * @return Either 1 or 0.
     */
    private int readBit() {
        if (bitOffset == 0) {
            currentByte = buf.readUnsignedByte();
        }

        int bit = (currentByte >> (7 - bitOffset)) & 1;
        bitOffset++;

        if (bitOffset == 8) {
            bitOffset = 0;
        }

        return bit;
    }

    public void resetBitReader() {
        currentByte = 0;
        bitOffset = 0;
    }
}
