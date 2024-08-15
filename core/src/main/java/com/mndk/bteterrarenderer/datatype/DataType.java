package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.*;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public interface DataType<T> {

    static <T> DataType<T> object(Supplier<T> defaultValueMaker) { return new ObjectType<>(defaultValueMaker); }
    static DataNumberType<Boolean> bool() { return DataTypeStorage.BOOLEAN; }
    static DataNumberType<Byte> int8() { return DataTypeStorage.BYTE; }
    static DataNumberType<UByte> uint8() { return DataTypeStorage.UBYTE; }
    static DataNumberType<Short> int16() { return DataTypeStorage.SHORT; }
    static DataNumberType<UShort> uint16() { return DataTypeStorage.USHORT; }
    static DataNumberType<Integer> int32() { return DataTypeStorage.INT; }
    static DataNumberType<UInt> uint32() { return DataTypeStorage.UINT; }
    static DataNumberType<Long> int64() { return DataTypeStorage.LONG; }
    static DataNumberType<ULong> uint64() { return DataTypeStorage.ULONG; }
    static DataNumberType<Float> float32() { return DataTypeStorage.FLOAT; }
    static DataNumberType<Double> float64() { return DataTypeStorage.DOUBLE; }
    static DataType<RawPointer> bytes(long size) { return new RawPointerType(size); }
    static DataType<String> string(int byteLength, Charset charset) { return new StringType(byteLength, charset); }
    static DataType<String> string(int byteLength) { return new StringType(byteLength, StandardCharsets.UTF_8); }

    static long toRaw(double value) { return Double.doubleToRawLongBits(value); }
    static double fromRaw(long raw) { return Double.longBitsToDouble(raw); }
    static int toRaw(float value) { return Float.floatToRawIntBits(value); }
    static float fromRaw(int raw) { return Float.intBitsToFloat(raw); }
    static byte toRaw(boolean value) { return (byte) (value ? 1 : 0); }
    static boolean fromRaw(byte raw) { return raw != 0; }

    static Endian endian() { return DataTypeStorage.ENDIAN; }
    static void endian(Endian endian) { DataTypeStorage.ENDIAN = endian; }

    // General conversions
    T parse(String value);
    T defaultValue();
    boolean equals(T left, T right);
    int hashCode(T value);
    String toString(T value);
    default DataNumberType<T> asNumber() { return (DataNumberType<T>) this; }

    // IO operations
    long byteSize();
    T read(RawPointer src);
    void write(RawPointer dst, T value);

    // Pointer operations
    Pointer<T> newOwned(T value);
    Pointer<T> newArray(int length);
    Pointer<T> castPointer(RawPointer pointer);
    default Pointer<T> newArray(long length) {
        if (length >> 32 == 0) return this.newArray((int) length);
        throw new IllegalArgumentException("Array length is too large");
    }
    default Pointer<T> newOwned() { return this.newOwned(this.defaultValue()); }
}
