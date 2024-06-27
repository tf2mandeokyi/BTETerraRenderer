package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class ObjectType<T> implements DataType<T, Object[]> {
    // IO operations
    @Override public long size() { throw new UnsupportedOperationException(); }
    @Override public T read(UByteArray array, long index, Endian endian) {
        throw new UnsupportedOperationException();
    }
    @Override public void write(UByteArray array, long index, T value, Endian endian) {
        throw new UnsupportedOperationException();
    }

    // General conversions
    @Override public T parse(String value) { throw new UnsupportedOperationException(); }
    @Override public boolean equals(T left, T right) { return left.equals(right); }
    @Override public int hashCode(T value) { return value.hashCode(); }
    @Override public String toString(T value) { return value.toString(); }

    // Array operations
    @Override public Object[] newArray(int length) { return new Object[length]; }
    @Override public T get(Object[] array, int index) { return BTRUtil.uncheckedCast(array[index]); }
    @Override public void set(Object[] array, int index, T value) { array[index] = value; }
    @Override public int length(Object[] array) { return array.length; }
    @Override public void copy(Object[] src, int srcIndex, Object[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(Object[] objects, int from, int to, @Nullable Comparator<T> comparator) {
        if (comparator == null) throw new IllegalArgumentException("Comparator cannot be null");
        Comparator<Object> c = (a, b) -> comparator.compare(BTRUtil.uncheckedCast(a), BTRUtil.uncheckedCast(b));
        Arrays.sort(objects, from, to, c);
    }
    @Override public int arrayHashCode(Object[] objects) { return Arrays.hashCode(objects); }
    @Override public boolean arrayEquals(Object[] array1, Object[] array2) { return Arrays.equals(array1, array2); }
}
