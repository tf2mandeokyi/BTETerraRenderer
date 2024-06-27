package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.IntComparator;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class IntType extends JavaNumberBridgeType<Integer, int[]> {
    // IO operations
    @Override public long size() { return 4; }
    @Override public Integer read(UByteArray array, long index, Endian endian) {
        return array.getUInt32(index, endian).intValue();
    }
    @Override public void write(UByteArray array, long index, Integer value, Endian endian) {
        array.setUInt32(index, UInt.of(value), endian);
    }

    // General conversions
    @Override public Integer parse(String value) { return Integer.parseInt(value); }
    @Override public String toHexString(Integer value) { return Integer.toHexString(value); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Integer lowest() { return Integer.MIN_VALUE; }
    @Override public Integer min() { return Integer.MIN_VALUE;}
    @Override public Integer max() { return Integer.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return this; }
    @Override public DataNumberType<?, ?> getUnsigned() { return DataType.uint32(); }

    // Arithmetic operations
    @Override public Integer add(Integer left, Integer right) { return left + right; }
    @Override public Integer sub(Integer left, Integer right) { return left - right; }
    @Override public Integer mul(Integer left, Integer right) { return left * right; }
    @Override public Integer div(Integer left, Integer right) { return left / right; }
    @Override public Integer mod(Integer left, Integer right) { return left % right; }
    @Override public Integer negate(Integer value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Integer left, Integer right) { return left <  right; }
    @Override public boolean le(Integer left, Integer right) { return left <= right; }
    @Override public boolean gt(Integer left, Integer right) { return left >  right; }
    @Override public boolean ge(Integer left, Integer right) { return left >= right; }

    // Math functions
    @Override public Integer abs(Integer value) { return Math.abs(value); }
    @Override public Integer floor(Integer value) { return value; }
    @Override public Integer sqrt(Integer value) { return (int) Math.sqrt(value); }

    // Bitwise operations
    @Override public Integer and(Integer left, Integer right) { return left & right; }
    @Override public Integer or(Integer left, Integer right) { return left | right; }
    @Override public Integer xor(Integer left, Integer right) { return left ^ right; }
    @Override public Integer not(Integer value) { return ~value; }
    @Override public Integer shl(Integer value, int shift) { return value << shift; }
    @Override public Integer shr(Integer value, int shift) { return value >> shift; }

    // Number conversions (incoming)
    @Override public <U> Integer from(DataCalculator<U> type, U value) { return type.toInt(value); }
    @Override public Integer from(int value) { return value; }
    @Override public Integer from(long value) { return (int) value; }
    @Override public Integer from(float value) { return (int) value; }
    @Override public Integer from(double value) { return (int) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Integer value) { return value != 0; }

    // Array operations
    @Override public int[] newArray(int length) { return new int[length]; }
    @Override public Integer get(int[] array, int index) { return array[index]; }
    @Override public void set(int[] array, int index, @Nullable Integer value) { array[index] = value == null ? 0 : value; }
    @Override public int length(int[] array) { return array.length; }
    @Override public void copy(int[] src, int srcIndex, int[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(int[] array, int from, int to, @Nullable Comparator<Integer> comparator) {
        IntComparator objectComparator = comparator == null ? Integer::compare : comparator::compare;
        Primitive.sort(array, from, to, objectComparator);
    }
    @Override public int arrayHashCode(int[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(int[] array1, int[] array2) { return Arrays.equals(array1, array2); }
}
