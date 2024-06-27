package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.ShortComparator;

import java.util.Arrays;
import java.util.Comparator;

public class ShortType extends JavaNumberBridgeType<Short, short[]> {
    // IO operations
    @Override public long size() { return 2; }
    @Override public Short read(UByteArray array, long index, Endian endian) {
        return array.getUInt16(index, endian).shortValue();
    }
    @Override public void write(UByteArray array, long index, Short value, Endian endian) {
        array.setUInt16(index, UShort.of(value), endian);
    }

    // General conversions
    @Override public Short parse(String value) { return Short.parseShort(value); }
    @Override public String toHexString(Short value) { return Integer.toHexString(value & 0xFFFF); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Short lowest() { return Short.MIN_VALUE; }
    @Override public Short min() { return Short.MIN_VALUE;}
    @Override public Short max() { return Short.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return this; }
    @Override public DataNumberType<?, ?> getUnsigned() { return DataType.uint16(); }

    // Arithmetic operations
    @Override public Short add(Short left, Short right) { return (short) (left + right); }
    @Override public Short sub(Short left, Short right) { return (short) (left - right); }
    @Override public Short mul(Short left, Short right) { return (short) (left * right); }
    @Override public Short div(Short left, Short right) { return (short) (left / right); }
    @Override public Short mod(Short left, Short right) { return (short) (left % right); }
    @Override public Short negate(Short value) { return (short) -value; }

    // Comparison operations
    @Override public boolean lt(Short left, Short right) { return left <  right; }
    @Override public boolean le(Short left, Short right) { return left <= right; }
    @Override public boolean gt(Short left, Short right) { return left >  right; }
    @Override public boolean ge(Short left, Short right) { return left >= right; }

    // Math functions
    @Override public Short abs(Short value) { return (short) Math.abs(value); }
    @Override public Short floor(Short value) { return value; }
    @Override public Short sqrt(Short value) { return (short) Math.sqrt(value); }

    // Bitwise operations
    @Override public Short and(Short left, Short right) { return (short) (left & right); }
    @Override public Short or(Short left, Short right) { return (short) (left | right); }
    @Override public Short xor(Short left, Short right) { return (short) (left ^ right); }
    @Override public Short not(Short value) { return (short) ~value; }
    @Override public Short shl(Short value, int shift) { return (short) (value << shift); }
    @Override public Short shr(Short value, int shift) { return (short) (value >> shift); }

    // Number conversions (incoming)
    @Override public <U> Short from(DataCalculator<U> type, U value) { return type.toShort(value); }
    @Override public Short from(int value) { return (short) value; }
    @Override public Short from(long value) { return (short) value; }
    @Override public Short from(float value) { return (short) value; }
    @Override public Short from(double value) { return (short) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Short value) { return value != 0; }

    // Array operations
    @Override public short[] newArray(int length) { return new short[length]; }
    @Override public Short get(short[] array, int index) { return array[index]; }
    @Override public void set(short[] array, int index, Short value) { array[index] = value; }
    @Override public int length(short[] array) { return array.length; }
    @Override public void copy(short[] src, int srcIndex, short[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(short[] array, int from, int to, Comparator<Short> comparator) {
        ShortComparator objectComparator = comparator == null ? Short::compare : comparator::compare;
        Primitive.sort(array, from, to, objectComparator);
    }
    @Override public int arrayHashCode(short[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(short[] array1, short[] array2) { return Arrays.equals(array1, array2); }
}
