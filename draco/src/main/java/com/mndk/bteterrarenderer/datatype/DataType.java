package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.*;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

import java.util.function.Supplier;

public interface DataType<T> {

    static <T> DataType<T> object(Supplier<T> defaultValueMaker) { return new ObjectType<>(defaultValueMaker); }
    static DataNumberType<Boolean> bool() { return DataNumberTypeManager.BOOLEAN; }
    static DataNumberType<Byte> int8() { return DataNumberTypeManager.BYTE; }
    static DataNumberType<UByte> uint8() { return DataNumberTypeManager.UBYTE; }
    static DataNumberType<Short> int16() { return DataNumberTypeManager.SHORT; }
    static DataNumberType<UShort> uint16() { return DataNumberTypeManager.USHORT; }
    static DataNumberType<Integer> int32() { return DataNumberTypeManager.INT; }
    static DataNumberType<UInt> uint32() { return DataNumberTypeManager.UINT; }
    static DataNumberType<Long> int64() { return DataNumberTypeManager.LONG; }
    static DataNumberType<ULong> uint64() { return DataNumberTypeManager.ULONG; }
    static DataNumberType<Float> float32() { return DataNumberTypeManager.FLOAT; }
    static DataNumberType<Double> float64() { return DataNumberTypeManager.DOUBLE; }

    static long toRaw(double value) { return Double.doubleToRawLongBits(value); }
    static double fromRaw(long raw) { return Double.longBitsToDouble(raw); }
    static int toRaw(float value) { return Float.floatToRawIntBits(value); }
    static float fromRaw(int raw) { return Float.intBitsToFloat(raw); }
    static byte toRaw(boolean value) { return (byte) (value ? 1 : 0); }
    static boolean fromRaw(byte raw) { return raw != 0; }

    static int intLimit(long value) {
        if((int) value == value) return (int) value;
        throw new IllegalArgumentException("Value is too large: " + value);
    }

    static Endian endian() { return DataNumberTypeManager.ENDIAN; }
    static void endian(Endian endian) { DataNumberTypeManager.ENDIAN = endian; }

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
    default Pointer<T> newArray(long length) { return this.newArray(DataType.intLimit(length)); }
    default Pointer<T> newOwned() { return this.newOwned(this.defaultValue()); }

    static <U> DataNumberType<U> biOp(DataNumberType<?> left, DataNumberType<?> right) {
        return DataNumberTypeManager.biOp(left, right);
    }
    static <L, R, V> V add(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.add(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> V sub(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.sub(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> V mul(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.mul(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> V div(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.div(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> V mod(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.mod(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> boolean equals(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.equals(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> boolean lt(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.lt(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> boolean le(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.le(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> boolean gt(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.gt(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> boolean ge(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.ge(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> V and(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.and(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> V or(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.or(type.from(leftType, left), type.from(rightType, right));
    }
    static <L, R, V> V xor(DataNumberType<L> leftType, L left, DataNumberType<R> rightType, R right) {
        DataNumberType<V> type = biOp(leftType, rightType);
        return type.xor(type.from(leftType, left), type.from(rightType, right));
    }
}
