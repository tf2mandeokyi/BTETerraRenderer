package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.DoubleComparator;

import java.util.Arrays;
import java.util.Comparator;

public class DoubleType extends JavaNumberBridgeType<Double, double[]> {
    // IO operations
    @Override public long size() { return 8; }
    @Override public Double read(UByteArray array, long index, Endian endian) {
        return Double.longBitsToDouble(array.getUInt64(index, endian).longValue());
    }
    @Override public void write(UByteArray array, long index, Double value, Endian endian) {
        array.setUInt64(index, ULong.of(Double.doubleToRawLongBits(value)), endian);
    }

    // General conversions
    @Override public Double parse(String value) { return Double.parseDouble(value); }
    @Override public String toHexString(Double value) { return Double.toHexString(value); }

    // Number properties
    @Override public boolean isIntegral() { return false; }
    @Override public Double lowest() { return -Double.MAX_VALUE; }
    @Override public Double min() { return Double.MIN_VALUE;}
    @Override public Double max() { return Double.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return this; }
    @Override public DataNumberType<?, ?> getUnsigned() { throw new UnsupportedOperationException(); }

    // Arithmetic operations
    @Override public Double add(Double left, Double right) { return left + right; }
    @Override public Double sub(Double left, Double right) { return left - right; }
    @Override public Double mul(Double left, Double right) { return left * right; }
    @Override public Double div(Double left, Double right) { return left / right; }
    @Override public Double mod(Double left, Double right) { return left % right; }
    @Override public Double negate(Double value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Double left, Double right) { return left <  right; }
    @Override public boolean le(Double left, Double right) { return left <= right; }
    @Override public boolean gt(Double left, Double right) { return left >  right; }
    @Override public boolean ge(Double left, Double right) { return left >= right; }

    // Math functions
    @Override public Double abs(Double value) { return Math.abs(value); }
    @Override public Double floor(Double value) { return Math.floor(value); }
    @Override public Double sqrt(Double value) { return Math.sqrt(value); }

    // Bitwise operations
    @Override public Double and(Double left, Double right) { throw new UnsupportedOperationException(); }
    @Override public Double or(Double left, Double right) { throw new UnsupportedOperationException(); }
    @Override public Double xor(Double left, Double right) { throw new UnsupportedOperationException(); }
    @Override public Double not(Double value) { throw new UnsupportedOperationException(); }
    @Override public Double shl(Double value, int shift) { throw new UnsupportedOperationException(); }
    @Override public Double shr(Double value, int shift) { throw new UnsupportedOperationException(); }

    // Number conversions (incoming)
    @Override public <U> Double from(DataCalculator<U> type, U value) { return type.toDouble(value); }
    @Override public Double from(int value) { return (double) value; }
    @Override public Double from(long value) { return (double) value; }
    @Override public Double from(float value) { return (double) value; }
    @Override public Double from(double value) { return value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Double value) { return value != 0; }

    // Array operations
    @Override public double[] newArray(int length) { return new double[length]; }
    @Override public Double get(double[] array, int index) { return array[index]; }
    @Override public void set(double[] array, int index, Double value) { array[index] = value; }
    @Override public int length(double[] array) { return array.length; }
    @Override public void copy(double[] src, int srcIndex, double[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(double[] array, int from, int to, Comparator<Double> comparator) {
        DoubleComparator objectComparator = comparator == null ? Double::compare : comparator::compare;
        Primitive.sort(array, from, to, objectComparator);
    }
    @Override public int arrayHashCode(double[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(double[] array1, double[] array2) { return Arrays.equals(array1, array2); }
}
