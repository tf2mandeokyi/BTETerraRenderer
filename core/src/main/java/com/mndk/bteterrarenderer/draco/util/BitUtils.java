package com.mndk.bteterrarenderer.draco.util;

import io.netty.buffer.ByteBuf;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BitUtils {

    public long decodeVariableLongLE(ByteBuf buf) throws DracoCompressionException {
        return readBase128LE(buf, 64);
    }

    public int decodeVariableIntegerLE(ByteBuf buf) throws DracoCompressionException {
        return (int) readBase128LE(buf, 32);
    }

    /**
     * @implNote Draco 26.1: <a href="https://google.github.io/draco/spec/#leb128">LEB128</a>
     */
    private long readBase128LE(ByteBuf buf, int maxBits) throws DracoCompressionException {
        // Coding of unsigned values.
        // 0-6 bit - data
        // 7 bit - next byte?
        long result = 0;
        int shift = 0;
        while(true) {
            if(shift > maxBits) {
                throw new DracoCompressionException("max bits exceeded: expected=" + maxBits + ", actual=" + shift);
            }
            short b = buf.readUnsignedByte();
            result |= (long) (b & 0x7F) << shift;
            shift += 7;
            if ((b & 0x80) == 0) {
                System.out.println("[IO] readBase128LE -> " + result);
                return result;
            }
        }
    }
}
