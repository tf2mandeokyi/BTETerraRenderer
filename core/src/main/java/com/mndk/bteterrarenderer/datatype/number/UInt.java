package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UInt extends CppNumber<UInt> {
    public static final UInt MIN = new UInt(0);
    public static final UInt MAX = new UInt(-1);
    private static final long MASK = 0xFFFFFFFFL;
    public static final UInt ZERO = new UInt(0);

    public static UInt of(int value) { return new UInt(value); }
    public static UInt of(long value) { return new UInt((int) value);}
    public static Stream<UInt> range(UInt startInclusive, UInt endExclusive) {
        return IntStream.range(startInclusive.value, endExclusive.value).mapToObj(UInt::of);
    }
    public static UInt[] array(Integer... values) { return Stream.of(values).map(UInt::of).toArray(UInt[]::new); }
    public static UInt[] array(Long... values) { return Stream.of(values).map(UInt::of).toArray(UInt[]::new); }
    public static List<UInt> list(Integer... values) { return Stream.of(values).map(UInt::of).collect(Collectors.toList()); }
    public static List<UInt> list(Long... values) { return Stream.of(values).map(UInt::of).collect(Collectors.toList()); }

    public static UInt max(UInt a, UInt b) { return a.compareTo(b) >= 0 ? a : b; }

    private final int value;

    @Override public boolean equals(UInt other) { return value == other.value; }
    @Override public int hashCode() { return Integer.hashCode(value); }
    @Override public String toString() { return Integer.toUnsignedString(value); }
    @Override public String toHexString() { return Integer.toHexString(value); }
    @Override public DataNumberType<UInt> getType() { return DataType.uint32(); }

    // Arithmetic operations
    @Override public UInt add(UInt other) { return new UInt(value + other.value); }
    @Override public UInt sub(UInt other) { return new UInt(value - other.value); }
    @Override public UInt mul(UInt other) { return new UInt(value * other.value); }
    @Override public UInt div(UInt other) { return new UInt(Integer.divideUnsigned(value, other.value)); }
    @Override public UInt mod(UInt other) { return new UInt(Integer.remainderUnsigned(value, other.value)); }
    @Override public UInt negate() { return new UInt(-value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull UInt other) { return Integer.compareUnsigned(value, other.value); }

    // Math functions
    @Override public UInt abs() { return this; }
    @Override public UInt floor() { return this; }
    @Override public UInt sqrt() { return new UInt((int) Math.sqrt(value & MASK)); }

    // Bitwise operations
    @Override public UInt and(UInt other) { return new UInt(value & other.value);}
    @Override public UInt or(UInt other) { return new UInt(value | other.value); }
    @Override public UInt xor(UInt other) { return new UInt(value ^ other.value); }
    @Override public UInt not() { return new UInt(~value); }
    @Override public UInt shl(int shift) { return new UInt(value << shift); }
    @Override public UInt shr(int shift) { return new UInt(value >>> shift); }

    @Override public boolean booleanValue() { return value != 0; }
    @Override public byte byteValue() { return (byte) value; }
    @Override public short shortValue() { return (short) value; }
    @Override public int intValue() { return value; }
    @Override public long longValue() { return value & MASK; }
    @Override public float floatValue() { return value & MASK; }
    @Override public double doubleValue() { return value & MASK; }
    @Override public UByte uByteValue() { return UByte.of((byte) value); }
    @Override public UShort uShortValue() { return UShort.of((short) value); }
    @Override public UInt uIntValue() { return this; }
    @Override public ULong uLongValue() { return ULong.of(value & MASK); }
}
