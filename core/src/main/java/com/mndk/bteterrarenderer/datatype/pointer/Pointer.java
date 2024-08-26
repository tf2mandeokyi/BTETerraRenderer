package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Pointer<T> {

    static <T> Pointer<T> nullPointer() { return new NullPointer<>(); }

    static Pointer<Boolean> wrap(boolean[] array, int offset) { return new BorrowedBooleanArray(array, offset); }
    static Pointer<Byte> wrap(byte[] array, int offset) { return new BorrowedByteArray(array, offset); }
    static Pointer<Short> wrap(short[] array, int offset) { return new BorrowedShortArray(array, offset); }
    static Pointer<Integer> wrap(int[] array, int offset) { return new BorrowedIntArray(array, offset); }
    static Pointer<Long> wrap(long[] array, int offset) { return new BorrowedLongArray(array, offset); }
    static Pointer<Float> wrap(float[] array, int offset) { return new BorrowedFloatArray(array, offset); }
    static Pointer<Double> wrap(double[] array, int offset) { return new BorrowedDoubleArray(array, offset); }
    static Pointer<Boolean> wrap(boolean[] array) { return new BorrowedBooleanArray(array, 0); }
    static Pointer<Byte> wrap(byte[] array) { return new BorrowedByteArray(array, 0); }
    static Pointer<Short> wrap(short[] array) { return new BorrowedShortArray(array, 0); }
    static Pointer<Integer> wrap(int[] array) { return new BorrowedIntArray(array, 0); }
    static Pointer<Long> wrap(long[] array) { return new BorrowedLongArray(array, 0); }
    static Pointer<Float> wrap(float[] array) { return new BorrowedFloatArray(array, 0); }
    static Pointer<Double> wrap(double[] array) { return new BorrowedDoubleArray(array, 0); }

    static Pointer<UByte> wrapUnsigned(byte[] array, int offset) { return new BorrowedUByteArray(array, offset); }
    static Pointer<UShort> wrapUnsigned(short[] array, int offset) { return new BorrowedUShortArray(array, offset); }
    static Pointer<UInt> wrapUnsigned(int[] array, int offset) { return new BorrowedUIntArray(array, offset); }
    static Pointer<ULong> wrapUnsigned(long[] array, int offset) { return new BorrowedULongArray(array, offset); }
    static Pointer<UByte> wrapUnsigned(byte[] array) { return new BorrowedUByteArray(array, 0); }
    static Pointer<UShort> wrapUnsigned(short[] array) { return new BorrowedUShortArray(array, 0); }
    static Pointer<UInt> wrapUnsigned(int[] array) { return new BorrowedUIntArray(array, 0); }
    static Pointer<ULong> wrapUnsigned(long[] array) { return new BorrowedULongArray(array, 0); }

    static <T> Pointer<T> wrap(DataType<T> type, Object[] array) {
        return new BorrowedObjectArray<>(type, array, 0);
    }
    static <T> Pointer<T> wrap(DataType<T> type, Object[] array, int offset) {
        return new BorrowedObjectArray<>(type, array, offset);
    }
    static <T> Pointer<T> wrapTyped(DataType<T> type, T[] array) {
        return new BorrowedObjectArray<>(type, array, 0);
    }
    static <T> Pointer<T> wrapTyped(DataType<T> type, T[] array, int offset) {
        return new BorrowedObjectArray<>(type, array, offset);
    }
    static <T> Pointer<T> wrap(DataType<T> type, Supplier<T> getter, Consumer<T> setter) {
        return new FunctionalPointer<>(type, getter, setter);
    }

    static Pointer<Boolean> newBool() { return new OwnedBoolean(false); }
    static Pointer<Byte> newByte() { return new OwnedByte((byte) 0); }
    static Pointer<UByte> newUByte() { return new OwnedUByte((byte) 0); }
    static Pointer<Short> newShort() { return new OwnedShort((short) 0); }
    static Pointer<UShort> newUShort() { return new OwnedUShort((short) 0); }
    static Pointer<Integer> newInt() { return new OwnedInt(0); }
    static Pointer<UInt> newUInt() { return new OwnedUInt(0); }
    static Pointer<Long> newLong() { return new OwnedLong(0); }
    static Pointer<ULong> newULong() { return new OwnedULong(0); }
    static Pointer<Float> newFloat() { return new OwnedFloat(0); }
    static Pointer<Double> newDouble() { return new OwnedDouble(0); }

    static Pointer<Boolean> newBool(boolean value) { return new OwnedBoolean(value); }
    static Pointer<Byte> newByte(byte value) { return new OwnedByte(value); }
    static Pointer<UByte> newUByte(byte value) { return new OwnedUByte(value); }
    static Pointer<UByte> newUByte(UByte value) { return new OwnedUByte(value); }
    static Pointer<Short> newShort(short value) { return new OwnedShort(value); }
    static Pointer<UShort> newUShort(short value) { return new OwnedUShort(value); }
    static Pointer<UShort> newUShort(UShort value) { return new OwnedUShort(value); }
    static Pointer<Integer> newInt(int value) { return new OwnedInt(value); }
    static Pointer<UInt> newUInt(int value) { return new OwnedUInt(value); }
    static Pointer<UInt> newUInt(UInt value) { return new OwnedUInt(value); }
    static Pointer<Long> newLong(long value) { return new OwnedLong(value); }
    static Pointer<ULong> newULong(long value) { return new OwnedULong(value); }
    static Pointer<ULong> newULong(ULong value) { return new OwnedULong(value); }
    static Pointer<Float> newFloat(float value) { return new OwnedFloat(value); }
    static Pointer<Double> newDouble(double value) { return new OwnedDouble(value); }
    static <T> Pointer<T> newObject(DataType<T> type, T value) {
        return new OwnedObject<>(type, value);
    }

    static Pointer<Boolean> newBoolArray(long size) { return new BorrowedBooleanArray(new boolean[DataType.intLimit(size)], 0); }
    static Pointer<Byte> newByteArray(long size) { return new BorrowedByteArray(new byte[DataType.intLimit(size)], 0); }
    static Pointer<UByte> newUByteArray(long size) { return new BorrowedUByteArray(new byte[DataType.intLimit(size)], 0); }
    static Pointer<Short> newShortArray(long size) { return new BorrowedShortArray(new short[DataType.intLimit(size)], 0); }
    static Pointer<UShort> newUShortArray(long size) { return new BorrowedUShortArray(new short[DataType.intLimit(size)], 0); }
    static Pointer<Integer> newIntArray(long size) { return new BorrowedIntArray(new int[DataType.intLimit(size)], 0); }
    static Pointer<UInt> newUIntArray(long size) { return new BorrowedUIntArray(new int[DataType.intLimit(size)], 0); }
    static Pointer<Long> newLongArray(long size) { return new BorrowedLongArray(new long[DataType.intLimit(size)], 0); }
    static Pointer<ULong> newULongArray(long size) { return new BorrowedULongArray(new long[DataType.intLimit(size)], 0); }
    static Pointer<Float> newFloatArray(long size) { return new BorrowedFloatArray(new float[DataType.intLimit(size)], 0); }
    static Pointer<Double> newDoubleArray(long size) { return new BorrowedDoubleArray(new double[DataType.intLimit(size)], 0); }
    static <T> Pointer<T> newObjectArray(DataType<T> type, long size) {
        return new BorrowedObjectArray<>(type, new Object[DataType.intLimit(size)], 0);
    }

    T get();
    T get(long index);
    void set(T value);
    void set(long index, T value);
    Pointer<T> add(long offset);
    DataType<T> getType();
    Object getOrigin();

    RawPointer asRaw();
    default RawPointer asRaw(long byteOffset) { return this.asRaw().rawAdd(byteOffset); }

    default void reset() { this.set(this.getType().defaultValue()); }
    default void reset(long offset) { this.add(offset).reset(); }
    default void set(Function<T, T> value) { this.set(value.apply(this.get())); }
    default void set(long offset, Function<T, T> value) { this.add(offset).set(value.apply(this.get())); }
    default void swap(long a, long b) {
        if(a == b) return;
        T temp = this.get(a);
        this.set(a, this.get(b));
        this.set(b, temp);
    }

    default <U> Pointer<U> asRawTo(DataType<U> type) { return type.castPointer(this.asRaw()); }
    default Pointer<Boolean> asRawToBool() { return this.asRaw().toBool(); }
    default Pointer<Byte> asRawToByte() { return this.asRaw().toByte(); }
    default Pointer<UByte> asRawToUByte() { return this.asRaw().toUByte(); }
    default Pointer<Short> asRawToShort() { return this.asRaw().toShort(); }
    default Pointer<UShort> asRawToUShort() { return this.asRaw().toUShort(); }
    default Pointer<Integer> asRawToInt() { return this.asRaw().toInt(); }
    default Pointer<UInt> asRawToUInt() { return this.asRaw().toUInt(); }
    default Pointer<Long> asRawToLong() { return this.asRaw().toLong(); }
    default Pointer<ULong> asRawToULong() { return this.asRaw().toULong(); }
    default Pointer<Float> asRawToFloat() { return this.asRaw().toFloat(); }
    default Pointer<Double> asRawToDouble() { return this.asRaw().toDouble(); }
}
