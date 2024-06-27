package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import net.mintern.primitive.Primitive;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class ULongType extends PredefinedDataNumberType<ULong, long[]> {
    // IO operations
    @Override public long size() { return 8; }
    @Override public ULong read(UByteArray array, long index, Endian endian) {
        return array.getUInt64(index, endian);
    }
    @Override public void write(UByteArray array, long index, ULong value, Endian endian) {
        array.setUInt64(index, value, endian);
    }

    // General conversions
    @Override public ULong parse(String value) { return ULong.of(Long.parseUnsignedLong(value)); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public ULong lowest() { return ULong.MIN; }
    @Override public ULong min() { return ULong.MIN; }
    @Override public ULong max() { return ULong.MAX; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return DataType.int64(); }
    @Override public DataNumberType<?, ?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> ULong from(DataCalculator<U> type, U value) { return type.toULong(value); }
    @Override public ULong from(int value) { return ULong.of(value); }
    @Override public ULong from(long value) { return ULong.of(value); }
    @Override public ULong from(float value) { return ULong.of((long) value); }
    @Override public ULong from(double value) { return ULong.of((long) value); }

    // Array operations
    @Override public long[] newArray(int length) { return new long[length]; }
    @Override public ULong get(long[] array, int index) { return ULong.of(array[index]); }
    @Override public void set(long[] array, int index, ULong value) { array[index] = value == null ? 0 : value.longValue(); }
    @Override public int length(long[] array) { return array.length; }
    @Override public void copy(long[] array, int srcIndex, long[] dest, int destIndex, int length) {
        System.arraycopy(array, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(long[] array, int from, int to, @Nullable Comparator<ULong> comparator) {
        Comparator<ULong> c = comparator == null ? Comparator.naturalOrder() : comparator;
        Primitive.sort(array, from, to, (a, b) -> c.compare(ULong.of(a), ULong.of(b)));
    }
    @Override public int arrayHashCode(long[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(long[] array1, long[] array2) { return Arrays.equals(array1, array2); }
}
