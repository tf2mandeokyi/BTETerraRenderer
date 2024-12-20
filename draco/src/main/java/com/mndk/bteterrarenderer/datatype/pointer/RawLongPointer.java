package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.Endian;

public interface RawLongPointer extends RawPointer {

    @Override default byte getRawByte(long byteIndex) {
        long longIndex = byteIndex / 8, byteInLong = byteIndex % 8;
        return (byte) (getRawLong(longIndex) >> ((DataType.endian() == Endian.BIG ? 7 - byteInLong : byteInLong) * 8));
    }
    @Override default short getRawShort(long shortIndex) {
        long longIndex = shortIndex / 4, shortInLong = shortIndex % 4;
        return (short) (getRawLong(longIndex) >> ((DataType.endian() == Endian.BIG ? 3 - shortInLong : shortInLong) * 16));
    }
    @Override default int getRawInt(long intIndex) {
        long longIndex = intIndex / 2, intInLong = intIndex % 2;
        return (int) (getRawLong(longIndex) >> ((DataType.endian() == Endian.BIG ? 1 - intInLong : intInLong) * 32));
    }

    @Override default void setRawByte(long byteIndex, byte value) {
        long longIndex = byteIndex / 8, byteInLong = byteIndex % 8;
        long shiftAmount = (DataType.endian() == Endian.BIG ? 7 - byteInLong : byteInLong) * 8;
        long result = getRawLong(longIndex) & ~(0xFFL << shiftAmount) | ((long) value << shiftAmount);
        setRawLong(longIndex, result);
    }
    @Override default void setRawShort(long shortIndex, short value) {
        long longIndex = shortIndex / 4, shortInLong = shortIndex % 4;
        long shiftAmount = (DataType.endian() == Endian.BIG ? 3 - shortInLong : shortInLong) * 16;
        long result = getRawLong(longIndex) & ~(0xFFFFL << shiftAmount) | ((long) value << shiftAmount);
        setRawLong(longIndex, result);
    }
    @Override default void setRawInt(long intIndex, int value) {
        long longIndex = intIndex / 2, intInLong = intIndex % 2;
        long shiftAmount = (DataType.endian() == Endian.BIG ? 1 - intInLong : intInLong) * 32;
        long result = getRawLong(longIndex) & ~(0xFFFFFFFFL << shiftAmount) | ((long) value << shiftAmount);
        setRawLong(longIndex, result);
    }
}
