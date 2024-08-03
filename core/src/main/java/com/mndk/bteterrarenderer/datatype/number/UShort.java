package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UShort extends CppNumber<UShort> {
    public static final UShort MIN = new UShort((short) 0);
    public static final UShort MAX = new UShort((short) -1);
    private static final int MASK = 0xFFFF;
    public static final UShort ZERO = new UShort((short) 0);

    public static UShort of(short value) { return new UShort(value); }
    public static UShort of(int value) { return new UShort((short) value); }
    public static UShort[] array(Integer... values) { return Stream.of(values).map(UShort::of).toArray(UShort[]::new); }
    public static List<UShort> list(Integer... values) { return Stream.of(values).map(UShort::of).collect(Collectors.toList()); }

    private final short value;

    @Override public boolean equals(UShort other) { return value == other.value; }
    @Override public int hashCode() { return Short.hashCode(value); }
    @Override public String toString() { return Integer.toString(value & MASK); }
    @Override public String toHexString() { return Integer.toHexString(value & MASK); }
    @Override public DataNumberType<UShort> getType() { return DataType.uint16(); }

    // Arithmetic operations
    @Override public UShort add(UShort other) { return new UShort((short) (value + other.value)); }
    @Override public UShort sub(UShort other) { return new UShort((short) (value - other.value)); }
    @Override public UShort mul(UShort other) { return new UShort((short) (value * other.value)); }
    @Override public UShort div(UShort other) { return new UShort((short) ((value & MASK) / (other.value & MASK))); }
    @Override public UShort mod(UShort other) { return new UShort((short) ((value & MASK) % (other.value & MASK))); }
    @Override public UShort negate() { return new UShort((short) -value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull UShort other) { return Integer.compare(value & MASK, other.value & MASK); }

    // Math functions
    @Override public UShort abs() { return this; }
    @Override public UShort floor() { return this; }
    @Override public UShort sqrt() { return new UShort((short) Math.sqrt(value & MASK)); }

    // Bitwise operations
    @Override public UShort and(UShort other) { return new UShort((short) (value & other.value));}
    @Override public UShort or(UShort other) { return new UShort((short) (value | other.value)); }
    @Override public UShort xor(UShort other) { return new UShort((short) (value ^ other.value)); }
    @Override public UShort not() { return new UShort((short) ~value); }
    @Override public UShort shl(int shift) { return new UShort((short) (value << shift)); }
    @Override public UShort shr(int shift) { return new UShort((short) ((value & MASK) >>> shift)); }

    @Override public boolean booleanValue() { return value != 0; }
    @Override public byte byteValue() { return (byte) value; }
    @Override public short shortValue() { return value; }
    @Override public int intValue() { return value & MASK; }
    @Override public long longValue() { return value & MASK; }
    @Override public float floatValue() { return value & MASK; }
    @Override public double doubleValue() { return value & MASK; }
    @Override public UByte uByteValue() { return UByte.of((byte) value); }
    @Override public UShort uShortValue() { return this; }
    @Override public UInt uIntValue() { return UInt.of(value & MASK); }
    @Override public ULong uLongValue() { return ULong.of(value & MASK); }
}
