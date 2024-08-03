package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.Endian;

public interface RawShortPointer extends RawPointer {

    int MASK = 0xFFFF;

    @Override default byte getRawByte(long byteIndex) {
        long shortIndex = byteIndex / 2, byteInShort = byteIndex % 2;
        return (byte) (getRawShort(shortIndex) >> ((DataType.endian() == Endian.BIG ? 1 - byteInShort : byteInShort) * 8));
    }
    @Override default int getRawInt(long intIndex) {
        int result = 0;
        if(DataType.endian() == Endian.BIG) {
            result |= (getRawShort(2 * intIndex + 1) & MASK);
            result |= (getRawShort(2 * intIndex)/**/ & MASK) << 16;
        } else {
            result |= (getRawShort(2 * intIndex)/**/ & MASK);
            result |= (getRawShort(2 * intIndex + 1) & MASK) << 16;
        }
        return result;
    }
    @Override default long getRawLong(long longIndex) {
        long result = 0;
        if(DataType.endian() == Endian.BIG) {
            result |= /*  */ (getRawShort(4 * longIndex + 3) & MASK);
            result |= (long) (getRawShort(4 * longIndex + 2) & MASK) << 16;
            result |= (long) (getRawShort(4 * longIndex + 1) & MASK) << 32;
            result |= (long) (getRawShort(4 * longIndex)/**/ & MASK) << 48;
        } else {
            result |= /*  */ (getRawShort(4 * longIndex)/**/ & MASK);
            result |= (long) (getRawShort(4 * longIndex + 1) & MASK) << 16;
            result |= (long) (getRawShort(4 * longIndex + 2) & MASK) << 32;
            result |= (long) (getRawShort(4 * longIndex + 3) & MASK) << 48;
        }
        return result;
    }

    @Override default void setRawByte(long byteIndex, byte value) {
        long shortIndex = byteIndex / 2, byteInShort = byteIndex % 2;
        long shiftAmount = (DataType.endian() == Endian.BIG ? 1 - byteInShort : byteInShort) * 8;
        short result = (short) (getRawShort(shortIndex) & ~(0xFF << shiftAmount) | (value << shiftAmount));
        setRawShort(shortIndex, result);
    }
    @Override default void setRawInt(long intIndex, int value) {
        if(DataType.endian() == Endian.BIG) {
            setRawShort(2 * intIndex + 1, (short) value);
            setRawShort(2 * intIndex,/**/ (short) (value >> 16));
        } else {
            setRawShort(2 * intIndex,/**/ (short) value);
            setRawShort(2 * intIndex + 1, (short) (value >> 16));
        }
    }
    @Override default void setRawLong(long longIndex, long value) {
        if(DataType.endian() == Endian.BIG) {
            setRawShort(4 * longIndex + 3, (short) value);
            setRawShort(4 * longIndex + 2, (short) (value >> 16));
            setRawShort(4 * longIndex + 1, (short) (value >> 32));
            setRawShort(4 * longIndex,/**/ (short) (value >> 48));
        } else {
            setRawShort(4 * longIndex,/**/ (short) value);
            setRawShort(4 * longIndex + 1, (short) (value >> 16));
            setRawShort(4 * longIndex + 2, (short) (value >> 32));
            setRawShort(4 * longIndex + 3, (short) (value >> 48));
        }
    }
}
