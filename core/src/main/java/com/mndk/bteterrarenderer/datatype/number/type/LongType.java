package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.LongComparator;

import java.util.Arrays;
import java.util.Comparator;

public class LongType extends JavaNumberBridgeType<Long, long[]> {
    // IO operations
    @Override public long size() { return 8; }
    @Override public Long read(UByteArray array, long index, Endian endian) {
        return array.getUInt64(index, endian).longValue();
    }
    @Override public void write(UByteArray array, long index, Long value, Endian endian) {
        array.setUInt64(index, ULong.of(value), endian);
    }

    // General conversions
    @Override public Long parse(String value) { return Long.parseLong(value); }
    @Override public String toHexString(Long value) { return Long.toHexString(value); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Long lowest() { return Long.MIN_VALUE; }
    @Override public Long min() { return Long.MIN_VALUE;}
    @Override public Long max() { return Long.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return this; }
    @Override public DataNumberType<?, ?> getUnsigned() { return DataType.uint64(); }

    // Arithmetic operations
    @Override public Long add(Long left, Long right) { return left + right; }
    @Override public Long sub(Long left, Long right) { return left - right; }
    @Override public Long mul(Long left, Long right) { return left * right; }
    @Override public Long div(Long left, Long right) { return left / right; }
    @Override public Long mod(Long left, Long right) { return left % right; }
    @Override public Long negate(Long value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Long left, Long right) { return left <  right; }
    @Override public boolean le(Long left, Long right) { return left <= right; }
    @Override public boolean gt(Long left, Long right) { return left >  right; }
    @Override public boolean ge(Long left, Long right) { return left >= right; }

    // Math functions
    @Override public Long abs(Long value) { return Math.abs(value); }
    @Override public Long floor(Long value) { return value; }
    @Override public Long sqrt(Long value) { return (long) Math.sqrt(value); }

    // Bitwise operations
    @Override public Long and(Long left, Long right) { return left & right; }
    @Override public Long or(Long left, Long right) { return left | right; }
    @Override public Long xor(Long left, Long right) { return left ^ right; }
    @Override public Long not(Long value) { return ~value; }
    @Override public Long shl(Long value, int shift) { return value << shift; }
    @Override public Long shr(Long value, int shift) { return value >> shift; }

    // Number conversions (incoming)
    @Override public <U> Long from(DataCalculator<U> type, U value) { return type.toLong(value); }
    @Override public Long from(int value) { return (long) value; }
    @Override public Long from(long value) { return value; }
    @Override public Long from(float value) { return (long) value; }
    @Override public Long from(double value) { return (long) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Long value) { return value != 0; }

    // Array operations
    @Override public long[] newArray(int length) { return new long[length]; }
    @Override public Long get(long[] array, int index) { return array[index]; }
    @Override public void set(long[] array, int index, Long value) { array[index] = value; }
    @Override public int length(long[] array) { return array.length; }
    @Override public void copy(long[] src, int srcIndex, long[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(long[] array, int from, int to, Comparator<Long> comparator) {
        LongComparator objectComparator = comparator == null ? Long::compare : comparator::compare;
        Primitive.sort(array, from, to, objectComparator);
    }
    @Override public int arrayHashCode(long[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(long[] array1, long[] array2) { return Arrays.equals(array1, array2); }
}
