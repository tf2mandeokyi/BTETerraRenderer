package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.*;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import net.mintern.primitive.Primitive;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

public class BooleanType implements DataNumberType<Boolean, boolean[]> {
    // IO operations
    @Override public long size() { return 1; }
    @Override public Boolean read(UByteArray array, long index, Endian endian) {
        return array.get(index).intValue() != 0;
    }
    @Override public void write(UByteArray array, long index, Boolean value, Endian endian) {
        array.set(index, (byte) (value ? 1 : 0));
    }

    // General conversions
    @Override public Boolean parse(String value) { return !value.equals("0"); }
    @Override public boolean equals(Boolean left, Boolean right) { return left.equals(right); }
    @Override public int hashCode(Boolean value) { return Boolean.hashCode(value); }
    @Override public String toString(Boolean value) { return value ? "1" : "0"; }
    @Override public String toHexString(Boolean value) { return value ? "1" : "0"; }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public Boolean lowest() { return false; }
    @Override public Boolean min() { return false; }
    @Override public Boolean max() { return true; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { throw new UnsupportedOperationException(); }
    @Override public DataNumberType<?, ?> getUnsigned() { return this; }

    // Arithmetic operations: Based on the C++ behavior of bool
    @Override public Boolean add(Boolean left, Boolean right) { return left || right; }
    @Override public Boolean sub(Boolean left, Boolean right) { return left ^ right; }
    @Override public Boolean mul(Boolean left, Boolean right) { return left && right; }
    @Override public Boolean div(Boolean left, Boolean right) {
        if(!right) throw new ArithmeticException("Division by zero");
        return left;
    }
    @Override public Boolean mod(Boolean left, Boolean right) {
        if(!right) throw new ArithmeticException("Division by zero");
        return false;
    }
    @Override public Boolean negate(Boolean value) { return value; }

    // Comparison operations
    @Override public int compareTo(Boolean left, Boolean right) { return left.compareTo(right); }
    @Override public boolean lt(Boolean left, Boolean right) { return !left &&  right; }
    @Override public boolean le(Boolean left, Boolean right) { return !left ||  right; }
    @Override public boolean gt(Boolean left, Boolean right) { return  left && !right; }
    @Override public boolean ge(Boolean left, Boolean right) { return  left || !right; }

    // Math functions
    @Override public Boolean abs(Boolean value) { return value; }
    @Override public Boolean floor(Boolean value) { return value; }
    @Override public Boolean sqrt(Boolean value) { return value; }

    // Bitwise operations
    @Override public Boolean and(Boolean left, Boolean right) { return left && right; }
    @Override public Boolean or(Boolean left, Boolean right) { return left || right; }
    @Override public Boolean xor(Boolean left, Boolean right) { return left ^ right; }
    @Override public Boolean not(Boolean value) { return !value; }
    @Override public Boolean shl(Boolean value, int shift) { return value; }
    @Override public Boolean shr(Boolean value, int shift) { return value && shift == 0; }

    // Number conversions (incoming)
    @Override public <U> Boolean from(DataCalculator<U> type, U value) { return type.toBoolean(value); }
    @Override public Boolean from(int value) { return value != 0; }
    @Override public Boolean from(long value) { return value != 0; }
    @Override public Boolean from(float value) { return value != 0; }
    @Override public Boolean from(double value) { return value != 0; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Boolean value) { return value; }
    @Override public byte toByte(Boolean value) { return (byte) (value ? 1 : 0); }
    @Override public short toShort(Boolean value) { return (short) (value ? 1 : 0); }
    @Override public int toInt(Boolean value) { return value ? 1 : 0; }
    @Override public long toLong(Boolean value) { return value ? 1 : 0; }
    @Override public float toFloat(Boolean value) { return value ? 1 : 0; }
    @Override public double toDouble(Boolean value) { return value ? 1 : 0; }
    @Override public UByte toUByte(Boolean value) { return UByte.of(value ? 1 : 0); }
    @Override public UShort toUShort(Boolean value) { return UShort.of(value ? 1 : 0); }
    @Override public UInt toUInt(Boolean value) { return UInt.of(value ? 1 : 0); }
    @Override public ULong toULong(Boolean value) { return ULong.of(value ? 1 : 0); }

    @Override public boolean[] newArray(int length) { return new boolean[length]; }
    @Override public Boolean get(boolean[] array, int index) { return array[index]; }
    @Override public void set(boolean[] array, int index, Boolean value) { array[index] = value != null && value; }
    @Override public int length(boolean[] array) { return array.length; }
    @Override public void copy(boolean[] array, int srcIndex, boolean[] dest, int destIndex, int length) {
        System.arraycopy(array, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(boolean[] array, int from, int to, @Nullable Comparator<Boolean> comparator) {
        Comparator<Boolean> c = comparator == null ? Comparator.naturalOrder() : comparator;
        Primitive.sort(array, from, to, c::compare);
    }
    @Override public int arrayHashCode(boolean[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(boolean[] array1, boolean[] array2) { return Arrays.equals(array1, array2); }
}
