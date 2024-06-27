package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.FloatComparator;

import java.util.Arrays;
import java.util.Comparator;

public class FloatType extends JavaNumberBridgeType<Float, float[]> {
    // IO operations
    @Override public long size() { return 4; }
    @Override public Float read(UByteArray array, long index, Endian endian) {
        return Float.intBitsToFloat(array.getUInt32(index, endian).intValue());
    }
    @Override public void write(UByteArray array, long index, Float value, Endian endian) {
        array.setUInt32(index, UInt.of(Float.floatToRawIntBits(value)), endian);
    }

    // General conversions
    @Override public Float parse(String value) { return Float.parseFloat(value); }
    @Override public String toHexString(Float value) { return Float.toHexString(value); }

    // Number properties
    @Override public boolean isIntegral() { return false; }
    @Override public Float lowest() { return -Float.MAX_VALUE; }
    @Override public Float min() { return Float.MIN_VALUE;}
    @Override public Float max() { return Float.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return this; }
    @Override public DataNumberType<?, ?> getUnsigned() { throw new UnsupportedOperationException(); }

    // Arithmetic operations
    @Override public Float add(Float left, Float right) { return left + right; }
    @Override public Float sub(Float left, Float right) { return left - right; }
    @Override public Float mul(Float left, Float right) { return left * right; }
    @Override public Float div(Float left, Float right) { return left / right; }
    @Override public Float mod(Float left, Float right) { return left % right; }
    @Override public Float negate(Float value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Float left, Float right) { return left <  right; }
    @Override public boolean le(Float left, Float right) { return left <= right; }
    @Override public boolean gt(Float left, Float right) { return left >  right; }
    @Override public boolean ge(Float left, Float right) { return left >= right; }

    // Math functions
    @Override public Float abs(Float value) { return Math.abs(value); }
    @Override public Float floor(Float value) { return (float) Math.floor(value); }
    @Override public Float sqrt(Float value) { return (float) Math.sqrt(value); }

    // Bitwise operations
    @Override public Float and(Float left, Float right) { throw new UnsupportedOperationException(); }
    @Override public Float or(Float left, Float right) { throw new UnsupportedOperationException(); }
    @Override public Float xor(Float left, Float right) { throw new UnsupportedOperationException(); }
    @Override public Float not(Float value) { throw new UnsupportedOperationException(); }
    @Override public Float shl(Float value, int shift) { throw new UnsupportedOperationException(); }
    @Override public Float shr(Float value, int shift) { throw new UnsupportedOperationException(); }

    // Number conversions (incoming)
    @Override public <U> Float from(DataCalculator<U> type, U value) { return type.toFloat(value); }
    @Override public Float from(int value) { return (float) value; }
    @Override public Float from(long value) { return (float) value; }
    @Override public Float from(float value) { return value; }
    @Override public Float from(double value) { return (float) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Float value) { return value != 0; }

    // Array operations
    @Override public float[] newArray(int length) { return new float[length]; }
    @Override public Float get(float[] array, int index) { return array[index]; }
    @Override public void set(float[] array, int index, Float value) { array[index] = value; }
    @Override public int length(float[] array) { return array.length; }
    @Override public void copy(float[] src, int srcIndex, float[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(float[] array, int from, int to, Comparator<Float> comparator) {
        FloatComparator objectComparator = comparator == null ? Float::compare : comparator::compare;
        Primitive.sort(array, from, to, objectComparator);
    }
    @Override public int arrayHashCode(float[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(float[] array1, float[] array2) { return Arrays.equals(array1, array2); }
}
