package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import net.mintern.primitive.Primitive;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class UIntType extends PredefinedDataNumberType<UInt, int[]> {
    // IO operations
    @Override public long size() { return 4; }
    @Override public UInt read(UByteArray array, long index, Endian endian) {
        return array.getUInt32(index, endian);
    }
    @Override public void write(UByteArray array, long index, UInt value, Endian endian) {
        array.setUInt32(index, value, endian);
    }

    // General conversions
    @Override public UInt parse(String value) { return UInt.of(Integer.parseUnsignedInt(value)); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public UInt lowest() { return UInt.MIN; }
    @Override public UInt min() { return UInt.MIN; }
    @Override public UInt max() { return UInt.MAX; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return DataType.int32(); }
    @Override public DataNumberType<?, ?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> UInt from(DataCalculator<U> type, U value) { return type.toUInt(value); }
    @Override public UInt from(int value) { return UInt.of(value); }
    @Override public UInt from(long value) { return UInt.of((int) value); }
    @Override public UInt from(float value) { return UInt.of((int) value); }
    @Override public UInt from(double value) { return UInt.of((int) value); }

    // Array operations
    @Override public int[] newArray(int length) { return new int[length]; }
    @Override public UInt get(int[] array, int index) { return UInt.of(array[index]); }
    @Override public void set(int[] array, int index, UInt value) { array[index] = value == null ? 0 : value.intValue(); }
    @Override public int length(int[] array) { return array.length; }
    @Override public void copy(int[] array, int srcIndex, int[] dest, int destIndex, int length) {
        System.arraycopy(array, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(int[] array, int from, int to, @Nullable Comparator<UInt> comparator) {
        Comparator<UInt> c = comparator == null ? Comparator.naturalOrder() : comparator;
        Primitive.sort(array, from, to, (a, b) -> c.compare(UInt.of(a), UInt.of(b)));
    }
    @Override public int arrayHashCode(int[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(int[] array1, int[] array2) { return Arrays.equals(array1, array2); }
}
