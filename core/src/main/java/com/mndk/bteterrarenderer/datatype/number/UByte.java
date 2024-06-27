package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UByte extends CppNumber<UByte> {
    public static final UByte MIN = new UByte((byte) 0);
    public static final UByte MAX = new UByte((byte) -1);
    public static final UByte ZERO = new UByte((byte) 0);
    private static final int MASK = 0xFF;

    public static UByte of(int value) { return new UByte((byte) value); }
    public static UByte[] array(Integer... values) { return Stream.of(values).map(UByte::of).toArray(UByte[]::new); }
    public static List<UByte> list(Integer... values) { return Stream.of(values).map(UByte::of).collect(Collectors.toList()); }

    private final byte value;

    @Override public boolean equals(UByte other) { return value == other.value; }
    @Override public int hashCode() { return Byte.hashCode(value); }
    @Override public String toString() { return Integer.toString(value & MASK); }
    @Override public String toHexString() { return Integer.toHexString(value & MASK); }
    @Override public DataNumberType<UByte, ?> getType() { return DataType.uint8(); }

    // Arithmetic operations
    @Override public UByte add(UByte other) { return new UByte((byte) (value + other.value)); }
    @Override public UByte sub(UByte other) { return new UByte((byte) (value - other.value)); }
    @Override public UByte mul(UByte other) { return new UByte((byte) (value * other.value)); }
    @Override public UByte div(UByte other) { return new UByte((byte) ((value & MASK) / (other.value & MASK))); }
    @Override public UByte mod(UByte other) { return new UByte((byte) ((value & MASK) % (other.value & MASK))); }
    @Override public UByte negate() { return new UByte((byte) -value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull UByte other) { return Integer.compare(value & MASK, other.value & MASK); }

    // Math functions
    @Override public UByte abs() { return this; }
    @Override public UByte floor() { return this; }
    @Override public UByte sqrt() { return new UByte((byte) Math.sqrt(value & MASK)); }

    // Bitwise operations
    @Override public UByte and(UByte other) { return new UByte((byte) (value & other.value));}
    @Override public UByte or(UByte other) { return new UByte((byte) (value | other.value)); }
    @Override public UByte xor(UByte other) { return new UByte((byte) (value ^ other.value)); }
    @Override public UByte not() { return new UByte((byte) ~value); }
    @Override public UByte shl(int shift) { return new UByte((byte) (value << shift)); }
    @Override public UByte shr(int shift) { return new UByte((byte) ((value & MASK) >>> shift)); }

    @Override public boolean booleanValue() { return value != 0; }
    @Override public byte byteValue() { return value; }
    @Override public short shortValue() { return (short) (value & MASK); }
    @Override public int intValue() { return value & MASK; }
    @Override public long longValue() { return value & MASK; }
    @Override public float floatValue() { return value & MASK; }
    @Override public double doubleValue() { return value & MASK; }
    @Override public UByte uByteValue() { return this; }
    @Override public UShort uShortValue() { return UShort.of((short) (value & MASK)); }
    @Override public UInt uIntValue() { return UInt.of(value & MASK); }
    @Override public ULong uLongValue() { return ULong.of(value & MASK); }
}
