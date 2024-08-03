package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Pointer<T> {

    static <T> Pointer<T> nullPointer() { return new NullPointer<>(); }

    static Pointer<Boolean> wrap(boolean[] array, int offset) { return new BorrowedBooleanArray(array, offset); }
    static Pointer<Byte> wrap(byte[] array, int offset) { return new BorrowedByteArray(array, offset); }
    static Pointer<Short> wrap(short[] array, int offset) { return new BorrowedShortArray(array, offset); }
    static Pointer<Integer> wrap(int[] array, int offset) { return new BorrowedIntArray(array, offset); }
    static Pointer<Long> wrap(long[] array, int offset) { return new BorrowedLongArray(array, offset); }
    static Pointer<Float> wrap(float[] array, int offset) { return new BorrowedFloatArray(array, offset); }
    static Pointer<Double> wrap(double[] array, int offset) { return new BorrowedDoubleArray(array, offset); }
    static Pointer<Boolean> wrap(boolean[] array) { return wrap(array, 0); }
    static Pointer<Byte> wrap(byte[] array) { return wrap(array, 0); }
    static Pointer<Short> wrap(short[] array) { return wrap(array, 0); }
    static Pointer<Integer> wrap(int[] array) { return wrap(array, 0); }
    static Pointer<Long> wrap(long[] array) { return wrap(array, 0); }
    static Pointer<Float> wrap(float[] array) { return wrap(array, 0); }
    static Pointer<Double> wrap(double[] array) { return wrap(array, 0); }

    static Pointer<UByte> wrapUnsigned(byte[] array, int offset) { return new BorrowedUByteArray(array, offset); }
    static Pointer<UShort> wrapUnsigned(short[] array, int offset) { return new BorrowedUShortArray(array, offset); }
    static Pointer<UInt> wrapUnsigned(int[] array, int offset) { return new BorrowedUIntArray(array, offset); }
    static Pointer<ULong> wrapUnsigned(long[] array, int offset) { return new BorrowedULongArray(array, offset); }
    static Pointer<UByte> wrapUnsigned(byte[] array) { return wrapUnsigned(array, 0); }
    static Pointer<UShort> wrapUnsigned(short[] array) { return wrapUnsigned(array, 0); }
    static Pointer<UInt> wrapUnsigned(int[] array) { return wrapUnsigned(array, 0); }
    static Pointer<ULong> wrapUnsigned(long[] array) { return wrapUnsigned(array, 0); }

    static <T> Pointer<T> wrap(DataType<T> type, Object[] array) { return wrap(type, array, 0); }
    static <T> Pointer<T> wrap(DataType<T> type, Object[] array, int offset) {
        return new BorrowedObjectArray<>(type, array, offset);
    }
    static <T> Pointer<T> wrapTyped(DataType<T> type, T[] array) { return wrap(type, array, 0); }
    static <T> Pointer<T> wrapTyped(DataType<T> type, T[] array, int offset) {
        return new BorrowedTypeArray<>(type, array, offset);
    }
    static <T> Pointer<T> wrap(DataType<T> type, Supplier<T> getter, Consumer<T> setter) {
        return new FunctionalPointer<>(type, getter, setter);
    }

    static <T> Pointer<T> newObject(DataType<T> type, T value) { return new OwnedObject<>(type, value); }
    static Pointer<Boolean> newBool() { return new OwnedBoolean(false); }
    static Pointer<Boolean> newBool(boolean value) { return new OwnedBoolean(value); }
    static Pointer<Byte> newByte() { return new OwnedByte((byte) 0); }
    static Pointer<Byte> newByte(byte value) { return new OwnedByte(value); }
    static Pointer<UByte> newUByte() { return new OwnedUByte((byte) 0); }
    static Pointer<UByte> newUByte(byte value) { return new OwnedUByte(value); }
    static Pointer<UByte> newUByte(UByte value) { return new OwnedUByte(value); }
    static Pointer<Short> newShort() { return new OwnedShort((short) 0); }
    static Pointer<Short> newShort(short value) { return new OwnedShort(value); }
    static Pointer<UShort> newUShort() { return new OwnedUShort((short) 0); }
    static Pointer<UShort> newUShort(short value) { return new OwnedUShort(value); }
    static Pointer<UShort> newUShort(UShort value) { return new OwnedUShort(value); }
    static Pointer<Integer> newInt() { return new OwnedInt(0); }
    static Pointer<Integer> newInt(int value) { return new OwnedInt(value); }
    static Pointer<UInt> newUInt() { return new OwnedUInt(0); }
    static Pointer<UInt> newUInt(int value) { return new OwnedUInt(value); }
    static Pointer<UInt> newUInt(UInt value) { return new OwnedUInt(value); }
    static Pointer<Long> newLong() { return new OwnedLong(0); }
    static Pointer<Long> newLong(long value) { return new OwnedLong(value); }
    static Pointer<ULong> newULong() { return new OwnedULong(0); }
    static Pointer<ULong> newULong(long value) { return new OwnedULong(value); }
    static Pointer<ULong> newULong(ULong value) { return new OwnedULong(value); }
    static Pointer<Float> newFloat() { return new OwnedFloat(0); }
    static Pointer<Float> newFloat(float value) { return new OwnedFloat(value); }
    static Pointer<Double> newDouble() { return new OwnedDouble(0); }
    static Pointer<Double> newDouble(double value) { return new OwnedDouble(value); }

    T get();
    T get(long index);
    void set(T value);
    void set(long index, T value);
    Pointer<T> add(long offset);
    DataType<T> getType();

    RawPointer asRaw();
    default RawPointer asRaw(long byteOffset) { return this.asRaw().rawAdd(byteOffset); }

    default void reset() { this.set(this.getType().defaultValue()); }
    default void reset(long offset) { this.add(offset).reset(); }
    default void set(Function<T, T> value) { this.set(value.apply(this.get())); }
    default void set(long offset, Function<T, T> value) { this.add(offset).set(value.apply(this.get())); }
    default Iterator<T> iterator(long length) {
        return new Iterator<T>() {
            private long index = 0;
            @Override public boolean hasNext() { return index < length; }
            @Override public T next() { return get(index++); }
        };
    }
    default Stream<T> stream(long length) {
        Spliterator<T> spliterator = Spliterators.spliterator(iterator(length), length, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }
    default void copyFrom(Pointer<T> source, int sourceLength) {
        if(sourceLength == 0) return;
        // In case of copying from the same array,
        // We first copy the source array to a temporary array, and then paste it back
        Object[] array = new Object[sourceLength];
        for(int i = 0; i < sourceLength; ++i) {
            array[i] = source.get(i);
        }
        for(int i = 0; i < sourceLength; ++i) {
            this.set(i, BTRUtil.<T>uncheckedCast(array[i]));
        }
    }
    default void sort(int length, @Nullable Comparator<T> comparator) {
        if(length <= 1) return;

        // Default behaviour: copy to temporary array, sort it, and paste it back
        // This is slow, so it is recommended to override this method
        // Copy to the temporary array
        Object[] array = new Object[length];
        for(int i = 0; i < length; ++i) {
            array[i] = this.get(i);
        }
        // Sort the temporary array
        Comparator<Object> c = comparator == null
                ? null : (a, b) -> comparator.compare(BTRUtil.uncheckedCast(a), BTRUtil.uncheckedCast(b));
        Arrays.sort(array, c);
        // Paste it back
        for(int i = 0; i < length; ++i) {
            this.set(i, BTRUtil.<T>uncheckedCast(array[i]));
        }
    }
    default boolean isSorted(long length, @Nullable Comparator<T> comparator) {
        if(length <= 1) return true;
        Comparator<T> realComparator = comparator != null ? comparator : this.getType().asNumber()::compareTo;
        T previous = this.get(0);
        for(int i = 1; i < length; ++i) {
            if(realComparator.compare(previous, this.get(i)) > 0) {
                return false;
            }
            previous = this.get(i);
        }
        return true;
    }
    default void reverse(long length) {
        for(long i = 0; i < length / 2; ++i) {
            this.swap(i, length - i - 1);
        }
    }
    default String contentToString(long length) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(this.get(i));
        }
        sb.append(']');
        return sb.toString();
    }
    default boolean contentEquals(Pointer<T> other, long length) {
        DataType<T> type = this.getType();
        for(int i = 0; i < length; ++i) {
            if(!type.equals(this.get(i), other.get(i))) {
                return false;
            }
        }
        return true;
    }
    default int contentHashCode(long length) {
        int hash = 0;
        DataType<T> type = this.getType();
        for(int i = 0; i < length; ++i) {
            hash = 31 * hash + type.hashCode(this.get(i));
        }
        return hash;
    }
    default boolean nextPermutation(long length, @Nullable Comparator<T> comparator) {
        if(length <= 1) return false;

        Comparator<T> realComparator = comparator != null ? comparator : this.getType().asNumber()::compareTo;
        long i = length - 1, j = length - 1;

        while(i > 0 && realComparator.compare(this.get(i - 1), this.get(i)) >= 0) i--;
        if(i == 0) return false;

        while(realComparator.compare(this.get(i - 1), this.get(j)) >= 0) j--;
        this.swap(i - 1, j);

        j = length - 1;
        for(; i < j; i++, j--) {
            this.swap(i, j);
        }
        return true;
    }
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
