package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import net.mintern.primitive.Primitive;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class UByteType extends PredefinedDataNumberType<UByte, byte[]> {
    // IO operations
    @Override public long size() { return 1; }
    @Override public UByte read(UByteArray array, long index, Endian endian) { return array.get(index); }
    @Override public void write(UByteArray array, long index, UByte value, Endian endian) { array.set(index, value); }

    // General conversions
    @Override public UByte parse(String value) { return UByte.of((byte) Short.parseShort(value)); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public UByte lowest() { return UByte.MIN; }
    @Override public UByte min() { return UByte.MIN;}
    @Override public UByte max() { return UByte.MAX; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return DataType.int8(); }
    @Override public DataNumberType<?, ?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> UByte from(DataCalculator<U> type, U value) { return type.toUByte(value); }
    @Override public UByte from(int value) { return UByte.of(value); }
    @Override public UByte from(long value) { return UByte.of((int) value); }
    @Override public UByte from(float value) { return UByte.of((int) value); }
    @Override public UByte from(double value) { return UByte.of((int) value); }

    // Array operations
    @Override public byte[] newArray(int length) { return new byte[length]; }
    @Override public UByte get(byte[] array, int index) { return UByte.of(array[index]); }
    @Override public void set(byte[] array, int index, UByte value) { array[index] = value == null ? 0 : value.byteValue(); }
    @Override public int length(byte[] array) { return array.length; }
    @Override public void copy(byte[] array, int srcIndex, byte[] dest, int destIndex, int length) {
        System.arraycopy(array, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(byte[] array, int from, int to, @Nullable Comparator<UByte> comparator) {
        Comparator<UByte> c = comparator == null ? Comparator.naturalOrder() : comparator;
        Primitive.sort(array, from, to, (a, b) -> c.compare(UByte.of(a), UByte.of(b)));
    }
    @Override public int arrayHashCode(byte[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(byte[] array1, byte[] array2) { return Arrays.equals(array1, array2); }
}
