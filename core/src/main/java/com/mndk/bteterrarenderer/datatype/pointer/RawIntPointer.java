package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.Endian;

public interface RawIntPointer extends RawPointer {

    long MASK = 0xFFFFFFFFL;

    @Override default byte getRawByte(long byteIndex) {
        long intIndex = byteIndex / 4, byteInInt = byteIndex % 4;
        return (byte) (getRawInt(intIndex) >> ((DataType.endian() == Endian.BIG ? 3 - byteInInt : byteInInt) * 8));
    }
    @Override default short getRawShort(long shortIndex) {
        long intIndex = shortIndex / 2, shortInInt = shortIndex % 2;
        return (short) (getRawInt(intIndex) >> ((DataType.endian() == Endian.BIG ? 1 - shortInInt : shortInInt) * 16));
    }
    @Override default long getRawLong(long longIndex) {
        long result = 0; boolean isBigEndian = DataType.endian() == Endian.BIG;
        if (isBigEndian) {
            result |= (getRawInt(2 * longIndex + 1) & MASK);
            result |= (getRawInt(2 * longIndex)/**/ & MASK) << 32;
        } else {
            result |= (getRawInt(2 * longIndex)/**/ & MASK);
            result |= (getRawInt(2 * longIndex + 1) & MASK) << 32;
        }
        return result;
    }

    @Override default void setRawByte(long byteIndex, byte value) {
        long intIndex = byteIndex / 4, byteInInt = byteIndex % 4;
        long shiftAmount = (DataType.endian() == Endian.BIG ? 3 - byteInInt : byteInInt) * 8;
        int result = getRawInt(intIndex) & ~(0xFF << shiftAmount) | (value << shiftAmount);
        setRawInt(intIndex, result);
    }
    @Override default void setRawShort(long shortIndex, short value) {
        long intIndex = shortIndex / 2, shortInInt = shortIndex % 2;
        long shiftAmount = (DataType.endian() == Endian.BIG ? 1 - shortInInt : shortInInt) * 16;
        int result = getRawInt(intIndex) & ~(0xFFFF << shiftAmount) | (value << shiftAmount);
        setRawInt(intIndex, result);
    }
    @Override default void setRawLong(long longIndex, long value) {
        boolean isBigEndian = DataType.endian() == Endian.BIG;
        if (isBigEndian) {
            setRawInt(2 * longIndex + 1, (int) value);
            setRawInt(2 * longIndex,/**/ (int) (value >>> 32));
        } else {
            setRawInt(2 * longIndex,/**/ (int) value);
            setRawInt(2 * longIndex + 1, (int) (value >>> 32));
        }
    }
}
