package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.Endian;

public interface RawBytePointer extends RawPointer {

    int MASK = 0xFF;

    @Override default short getRawShort(long shortIndex) {
        int result = 0;
        if (DataType.endian() == Endian.BIG) {
            result |= (getRawByte(2 * shortIndex + 1) & MASK);
            result |= (getRawByte(2 * shortIndex)/**/ & MASK) << 8;
        } else {
            result |= (getRawByte(2 * shortIndex)/**/ & MASK);
            result |= (getRawByte(2 * shortIndex + 1) & MASK) << 8;
        }
        return (short) result;
    }
    @Override default int getRawInt(long intIndex) {
        int result = 0;
        if (DataType.endian() == Endian.BIG) {
            result |= (getRawByte(4 * intIndex + 3) & MASK);
            result |= (getRawByte(4 * intIndex + 2) & MASK) << 8;
            result |= (getRawByte(4 * intIndex + 1) & MASK) << 16;
            result |= (getRawByte(4 * intIndex)/**/ & MASK) << 24;
        } else {
            result |= (getRawByte(4 * intIndex)/**/ & MASK);
            result |= (getRawByte(4 * intIndex + 1) & MASK) << 8;
            result |= (getRawByte(4 * intIndex + 2) & MASK) << 16;
            result |= (getRawByte(4 * intIndex + 3) & MASK) << 24;
        }
        return result;
    }
    @Override default long getRawLong(long longIndex) {
        long result = 0;
        if (DataType.endian() == Endian.BIG) {
            result |= /*  */ (getRawByte(8 * longIndex + 7) & MASK);
            result |= (long) (getRawByte(8 * longIndex + 6) & MASK) << 8;
            result |= (long) (getRawByte(8 * longIndex + 5) & MASK) << 16;
            result |= (long) (getRawByte(8 * longIndex + 4) & MASK) << 24;
            result |= (long) (getRawByte(8 * longIndex + 3) & MASK) << 32;
            result |= (long) (getRawByte(8 * longIndex + 2) & MASK) << 40;
            result |= (long) (getRawByte(8 * longIndex + 1) & MASK) << 48;
            result |= (long) (getRawByte(8 * longIndex)/**/ & MASK) << 56;
        } else {
            result |= /*  */ (getRawByte(8 * longIndex)/**/ & MASK);
            result |= (long) (getRawByte(8 * longIndex + 1) & MASK) << 8;
            result |= (long) (getRawByte(8 * longIndex + 2) & MASK) << 16;
            result |= (long) (getRawByte(8 * longIndex + 3) & MASK) << 24;
            result |= (long) (getRawByte(8 * longIndex + 4) & MASK) << 32;
            result |= (long) (getRawByte(8 * longIndex + 5) & MASK) << 40;
            result |= (long) (getRawByte(8 * longIndex + 6) & MASK) << 48;
            result |= (long) (getRawByte(8 * longIndex + 7) & MASK) << 56;
        }
        return result;
    }

    @Override default void setRawShort(long shortIndex, short value) {
        if (DataType.endian() == Endian.BIG) {
            setRawByte(2 * shortIndex + 1, (byte) value);
            setRawByte(2 * shortIndex,/**/ (byte) (value >>> 8));
        } else {
            setRawByte(2 * shortIndex,/**/ (byte) value);
            setRawByte(2 * shortIndex + 1, (byte) (value >>> 8));
        }
    }
    @Override default void setRawInt(long intIndex, int value) {
        if (DataType.endian() == Endian.BIG) {
            setRawByte(4 * intIndex + 3, (byte) value);
            setRawByte(4 * intIndex + 2, (byte) (value >>> 8));
            setRawByte(4 * intIndex + 1, (byte) (value >>> 16));
            setRawByte(4 * intIndex,/**/ (byte) (value >>> 24));
        } else {
            setRawByte(4 * intIndex,/**/ (byte) value);
            setRawByte(4 * intIndex + 1, (byte) (value >>> 8));
            setRawByte(4 * intIndex + 2, (byte) (value >>> 16));
            setRawByte(4 * intIndex + 3, (byte) (value >>> 24));
        }
    }
    @Override default void setRawLong(long longIndex, long value) {
        if (DataType.endian() == Endian.BIG) {
            setRawByte(8 * longIndex + 7, (byte) value);
            setRawByte(8 * longIndex + 6, (byte) (value >>> 8));
            setRawByte(8 * longIndex + 5, (byte) (value >>> 16));
            setRawByte(8 * longIndex + 4, (byte) (value >>> 24));
            setRawByte(8 * longIndex + 3, (byte) (value >>> 32));
            setRawByte(8 * longIndex + 2, (byte) (value >>> 40));
            setRawByte(8 * longIndex + 1, (byte) (value >>> 48));
            setRawByte(8 * longIndex,/**/ (byte) (value >>> 56));
        } else {
            setRawByte(8 * longIndex,/**/ (byte) value);
            setRawByte(8 * longIndex + 1, (byte) (value >>> 8));
            setRawByte(8 * longIndex + 2, (byte) (value >>> 16));
            setRawByte(8 * longIndex + 3, (byte) (value >>> 24));
            setRawByte(8 * longIndex + 4, (byte) (value >>> 32));
            setRawByte(8 * longIndex + 5, (byte) (value >>> 40));
            setRawByte(8 * longIndex + 6, (byte) (value >>> 48));
            setRawByte(8 * longIndex + 7, (byte) (value >>> 56));
        }
    }
}
