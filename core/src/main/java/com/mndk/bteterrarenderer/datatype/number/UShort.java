package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UShort extends CppNumber<UShort> {

    private static final UShort[] CACHE = new UShort[256];
    static { for (short i = 0; i < 256; ++i) CACHE[i] = new UShort(i); }

    public static final UShort MIN = of((short) 0);
    public static final UShort MAX = of((short) -1);
    private static final int MASK = 0xFFFF;
    public static final UShort ZERO = of((short) 0);

    public static UShort of(int value) { return of((short) value); }
    public static UShort of(short value) { return (value & 0xFF) == value ? CACHE[value] : new UShort(value); }

    private final short value;

    @Override public boolean equals(UShort other) { return value == other.value; }
    @Override public int hashCode() { return Short.hashCode(value); }
    @Override public String toString() { return Integer.toString(value & MASK); }
    @Override public String toHexString() { return Integer.toHexString(value & MASK); }
    @Override public DataNumberType<UShort> getType() { return DataType.uint16(); }

    // Arithmetic operations
    @Override public UShort add(UShort other) { return of((short) (value + other.value)); }
    @Override public UShort sub(UShort other) { return of((short) (value - other.value)); }
    @Override public UShort mul(UShort other) { return of((short) (value * other.value)); }
    @Override public UShort div(UShort other) { return of((short) ((value & MASK) / (other.value & MASK))); }
    @Override public UShort mod(UShort other) { return of((short) ((value & MASK) % (other.value & MASK))); }
    @Override public UShort negate() { return of((short) -value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull UShort other) { return Integer.compare(value & MASK, other.value & MASK); }

    // Math functions
    @Override public UShort abs() { return this; }
    @Override public UShort floor() { return this; }
    @Override public UShort sqrt() { return of((short) Math.sqrt(value & MASK)); }

    // Bitwise operations
    @Override public UShort and(UShort other) { return of((short) (value & other.value));}
    @Override public UShort or(UShort other) { return of((short) (value | other.value)); }
    @Override public UShort xor(UShort other) { return of((short) (value ^ other.value)); }
    @Override public UShort not() { return of((short) ~value); }
    @Override public UShort shl(int shift) { return of((short) (value << shift)); }
    @Override public UShort shr(int shift) { return of((short) ((value & MASK) >>> shift)); }

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
