package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.Endian;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;

public interface RawPointer {

    static RawPointer newArray(long byteSize) { return Pointer.newByteArray(byteSize).asRaw(); }

    default RawPointer rawAdd(long byteOffset) { return toUByte().add(byteOffset).asRaw(); }

    Object getOrigin();
    byte getRawByte(long byteIndex);
    short getRawShort(long shortIndex);
    int getRawInt(long intIndex);
    long getRawLong(long longIndex);
    default byte getRawByte() { return getRawByte(0); }
    default short getRawShort() { return getRawShort(0); }
    default int getRawInt() { return getRawInt(0); }
    default long getRawLong() { return getRawLong(0); }

    default int getRawInt24() { return getRawInt24(0); }
    default int getRawInt24(long int24Index) {
        int result = 0;
        if(DataType.endian() == Endian.BIG) {
            result |= (getRawByte(3 * int24Index + 2) & 0xFF);
            result |= (getRawByte(3 * int24Index + 1) & 0xFF) << 8;
            result |= (getRawByte(3 * int24Index)/**/ & 0xFF) << 16;
        } else {
            result |= (getRawByte(3 * int24Index)/**/ & 0xFF);
            result |= (getRawByte(3 * int24Index + 1) & 0xFF) << 8;
            result |= (getRawByte(3 * int24Index + 2) & 0xFF) << 16;
        }
        return result;
    }

    default UByte getRawUByte(long byteIndex) { return UByte.of(getRawByte(byteIndex)); }
    default UShort getRawUShort(long shortIndex) { return UShort.of(getRawShort(shortIndex)); }
    default UInt getRawUInt24(long int24Index) { return UInt.of(getRawInt24(int24Index)); }
    default UInt getRawUInt(long intIndex) { return UInt.of(getRawInt(intIndex)); }
    default ULong getRawULong(long longIndex) { return ULong.of(getRawLong(longIndex)); }
    default UByte getRawUByte() { return UByte.of(getRawByte()); }
    default UShort getRawUShort() { return UShort.of(getRawShort()); }
    default UInt getRawUInt24() { return UInt.of(getRawInt24()); }
    default UInt getRawUInt() { return UInt.of(getRawInt()); }
    default ULong getRawULong() { return ULong.of(getRawLong()); }

    void setRawByte(long byteIndex, byte value);
    void setRawShort(long shortIndex, short value);
    void setRawInt(long intIndex, int value);
    void setRawLong(long longIndex, long value);
    default void setRawByte(byte value) { setRawByte(0, value); }
    default void setRawShort(short value) { setRawShort(0, value); }
    default void setRawInt(int value) { setRawInt(0, value); }
    default void setRawLong(long value) { setRawLong(0, value); }

    default void setRawInt24(int value) { setRawInt24(0, value); }
    default void setRawInt24(long index, int value) {
        if(DataType.endian() == Endian.BIG) {
            setRawByte(3 * index + 2, (byte) value);
            setRawByte(3 * index + 1, (byte) (value >> 8));
            setRawByte(3 * index,/**/ (byte) (value >> 16));
        } else {
            setRawByte(3 * index,/**/ (byte) value);
            setRawByte(3 * index + 1, (byte) (value >> 8));
            setRawByte(3 * index + 2, (byte) (value >> 16));
        }
    }

    default void setRawByte(long byteIndex, UByte value) { setRawByte(byteIndex, value.byteValue()); }
    default void setRawShort(long shortIndex, UShort value) { setRawShort(shortIndex, value.shortValue()); }
    default void setRawInt24(long int24Index, UInt value) { setRawInt24(int24Index, value.intValue()); }
    default void setRawInt(long intIndex, UInt value) { setRawInt(intIndex, value.intValue()); }
    default void setRawLong(long longIndex, ULong value) { setRawLong(longIndex, value.longValue()); }
    default void setRawByte(UByte value) { setRawByte(0, value.byteValue()); }
    default void setRawShort(UShort value) { setRawShort(0, value.shortValue()); }
    default void setRawInt24(UInt value) { setRawInt24(0, value.intValue()); }
    default void setRawInt(UInt value) { setRawInt(0, value.intValue()); }
    default void setRawLong(ULong value) { setRawLong(0, value.longValue()); }

    default <U> Pointer<U> toType(DataType<U> type) { return type.castPointer(this); }
    default Pointer<Boolean> toBool() { return new CastedAsBooleanPointer(this, 0); }
    default Pointer<Byte> toByte() { return new CastedAsBytePointer(this, 0); }
    default Pointer<UByte> toUByte() { return new CastedAsUBytePointer(this, 0); }
    default Pointer<Short> toShort() { return new CastedAsShortPointer(this, 0); }
    default Pointer<UShort> toUShort() { return new CastedAsUShortPointer(this, 0); }
    default Pointer<Integer> toInt() { return new CastedAsIntPointer(this, 0); }
    default Pointer<UInt> toUInt() { return new CastedAsUIntPointer(this, 0); }
    default Pointer<Long> toLong() { return new CastedAsLongPointer(this, 0); }
    default Pointer<ULong> toULong() { return new CastedAsULongPointer(this, 0); }
    default Pointer<Float> toFloat() { return new CastedAsFloatPointer(this, 0); }
    default Pointer<Double> toDouble() { return new CastedAsDoublePointer(this, 0); }
}
