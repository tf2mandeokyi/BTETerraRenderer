package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import net.mintern.primitive.Primitive;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class UShortType extends PredefinedDataNumberType<UShort, short[]> {
    // IO operations
    @Override public long size() { return 2; }
    @Override public UShort read(UByteArray array, long index, Endian endian) {
        return array.getUInt16(index, endian);
    }
    @Override public void write(UByteArray array, long index, UShort value, Endian endian) {
        array.setUInt16(index, value, endian);
    }

    // General conversions
    @Override public UShort parse(String value) { return UShort.of((short) Integer.parseInt(value)); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public UShort lowest() { return UShort.MIN; }
    @Override public UShort min() { return UShort.MIN; }
    @Override public UShort max() { return UShort.MAX; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return DataType.int16(); }
    @Override public DataNumberType<?, ?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> UShort from(DataCalculator<U> type, U value) { return type.toUShort(value); }
    @Override public UShort from(int value) { return UShort.of(value); }
    @Override public UShort from(long value) { return UShort.of((int) value); }
    @Override public UShort from(float value) { return UShort.of((int) value); }
    @Override public UShort from(double value) { return UShort.of((int) value); }

    // Array operations
    @Override public short[] newArray(int length) { return new short[length]; }
    @Override public UShort get(short[] array, int index) { return UShort.of(array[index]); }
    @Override public void set(short[] array, int index, UShort value) { array[index] = value == null ? 0 : value.shortValue(); }
    @Override public int length(short[] array) { return array.length; }
    @Override public void copy(short[] array, int srcIndex, short[] dest, int destIndex, int length) {
        System.arraycopy(array, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(short[] array, int from, int to, @Nullable Comparator<UShort> comparator) {
        Comparator<UShort> c = comparator == null ? Comparator.naturalOrder() : comparator;
        Primitive.sort(array, from, to, (a, b) -> c.compare(UShort.of(a), UShort.of(b)));
    }
    @Override public int arrayHashCode(short[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(short[] array1, short[] array2) { return Arrays.equals(array1, array2); }
}
