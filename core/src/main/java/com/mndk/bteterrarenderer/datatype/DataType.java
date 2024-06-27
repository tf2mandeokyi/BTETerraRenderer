package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.array.StringType;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.array.UByteArrayType;
import com.mndk.bteterrarenderer.datatype.number.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface DataType<T, TArray> extends DataArrayManager<T, TArray>, DataIOManager<T> {

    static <T> DataType<T, Object[]> object() { return BTRUtil.uncheckedCast(DataTypes.DEFAULT); }
    static DataNumberType<Boolean, boolean[]> bool() { return DataTypes.BOOLEAN; }
    static DataNumberType<Byte, byte[]> int8() { return DataTypes.BYTE; }
    static DataNumberType<UByte, byte[]> uint8() { return DataTypes.UBYTE; }
    static DataNumberType<Short, short[]> int16() { return DataTypes.SHORT; }
    static DataNumberType<UShort, short[]> uint16() { return DataTypes.USHORT; }
    static DataNumberType<Integer, int[]> int32() { return DataTypes.INT; }
    static DataNumberType<UInt, int[]> uint32() { return DataTypes.UINT; }
    static DataNumberType<Long, long[]> int64() { return DataTypes.LONG; }
    static DataNumberType<ULong, long[]> uint64() { return DataTypes.ULONG; }
    static DataNumberType<Float, float[]> float32() { return DataTypes.FLOAT; }
    static DataNumberType<Double, double[]> float64() { return DataTypes.DOUBLE; }
    static DataType<UByteArray, Object[]> bytes(long size) { return new UByteArrayType(size); }
    static DataType<String, Object[]> string(int byteLength, Charset charset) {
        return new StringType(byteLength, charset);
    }
    static DataType<String, Object[]> string(int byteLength) {
        return new StringType(byteLength, StandardCharsets.UTF_8);
    }

    // General conversions
    T parse(String value);
    boolean equals(T left, T right);
    int hashCode(T value);
    String toString(T value);
}
