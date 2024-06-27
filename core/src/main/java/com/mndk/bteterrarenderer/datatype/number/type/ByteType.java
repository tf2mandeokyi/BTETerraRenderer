package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.Endian;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.ByteComparator;

import java.util.Arrays;
import java.util.Comparator;

public class ByteType extends JavaNumberBridgeType<Byte, byte[]> {
    // IO operations
    @Override public long size() { return 1; }
    @Override public Byte read(UByteArray array, long index, Endian endian) { return array.get(index).byteValue(); }
    @Override public void write(UByteArray array, long index, Byte value, Endian endian) { array.set(index, value); }

    // General conversions
    @Override public Byte parse(String value) { return Byte.parseByte(value); }
    @Override public String toHexString(Byte value) { return Integer.toHexString(value & 0xFF); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Byte lowest() { return Byte.MIN_VALUE; }
    @Override public Byte min() { return Byte.MIN_VALUE;}
    @Override public Byte max() { return Byte.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?, ?> getSigned() { return this; }
    @Override public DataNumberType<?, ?> getUnsigned() { return DataType.uint8(); }

    // Arithmetic operations
    @Override public Byte add(Byte left, Byte right) { return (byte) (left + right); }
    @Override public Byte sub(Byte left, Byte right) { return (byte) (left - right); }
    @Override public Byte mul(Byte left, Byte right) { return (byte) (left * right); }
    @Override public Byte div(Byte left, Byte right) { return (byte) (left / right); }
    @Override public Byte mod(Byte left, Byte right) { return (byte) (left % right); }
    @Override public Byte negate(Byte value) { return (byte) -value; }

    // Comparison operations
    @Override public boolean lt(Byte left, Byte right) { return left <  right; }
    @Override public boolean le(Byte left, Byte right) { return left <= right; }
    @Override public boolean gt(Byte left, Byte right) { return left >  right; }
    @Override public boolean ge(Byte left, Byte right) { return left >= right; }

    // Math functions
    @Override public Byte abs(Byte value) { return (byte) Math.abs(value); }
    @Override public Byte floor(Byte value) { return value; }
    @Override public Byte sqrt(Byte value) { return (byte) Math.sqrt(value); }

    // Bitwise operations
    @Override public Byte and(Byte left, Byte right) { return (byte) (left & right); }
    @Override public Byte or(Byte left, Byte right) { return (byte) (left | right); }
    @Override public Byte xor(Byte left, Byte right) { return (byte) (left ^ right); }
    @Override public Byte not(Byte value) { return (byte) ~value; }
    @Override public Byte shl(Byte value, int shift) { return (byte) (value << shift); }
    @Override public Byte shr(Byte value, int shift) { return (byte) (value >> shift); }

    // Number conversions (incoming)
    @Override public <U> Byte from(DataCalculator<U> type, U value) { return type.toByte(value); }
    @Override public Byte from(int value) { return (byte) value; }
    @Override public Byte from(long value) { return (byte) value; }
    @Override public Byte from(float value) { return (byte) value; }
    @Override public Byte from(double value) { return (byte) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Byte value) { return value != 0; }

    // Array operations
    @Override public byte[] newArray(int length) { return new byte[length]; }
    @Override public Byte get(byte[] array, int index) { return array[index]; }
    @Override public void set(byte[] array, int index, Byte value) { array[index] = value; }
    @Override public int length(byte[] array) { return array.length; }
    @Override public void copy(byte[] src, int srcIndex, byte[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override public void sort(byte[] array, int from, int to, Comparator<Byte> comparator) {
        ByteComparator objectComparator = comparator == null ? Byte::compare : comparator::compare;
        Primitive.sort(array, from, to, objectComparator);
    }
    @Override public int arrayHashCode(byte[] array) { return Arrays.hashCode(array); }
    @Override public boolean arrayEquals(byte[] array1, byte[] array2) { return Arrays.equals(array1, array2); }
}
