package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UByte extends CppNumber<UByte> {

    // 256 isn't that big of a number, so we can make a cache table for all possible values
    private static final UByte[] CACHE = new UByte[256];
    private static final int CACHE_OFFSET = 128;
    static { for(int i = 0; i < 256; ++i) CACHE[i] = new UByte((byte) (i - CACHE_OFFSET)); }

    public static final UByte MIN = of((byte) 0);
    public static final UByte MAX = of((byte) -1);
    public static final UByte ZERO = of((byte) 0);
    private static final int MASK = 0xFF;

    public static UByte of(int value) { return of((byte) value); }
    public static UByte of(byte value) { return CACHE[value + CACHE_OFFSET]; }
    public static UByte min(UByte a, UByte b) { return a.compareTo(b) <= 0 ? a : b; }
    public static UByte max(UByte a, UByte b) { return a.compareTo(b) >= 0 ? a : b; }

    private final byte value;

    @Override public boolean equals(UByte other) { return value == other.value; }
    @Override public int hashCode() { return Byte.hashCode(value); }
    @Override public String toString() { return Integer.toString(value & MASK); }
    @Override public String toHexString() { return Integer.toHexString(value & MASK); }
    @Override public DataNumberType<UByte> getType() { return DataType.uint8(); }

    // Arithmetic operations
    @Override public UByte add(UByte other) { return of((byte) (value + other.value)); }
    @Override public UByte sub(UByte other) { return of((byte) (value - other.value)); }
    @Override public UByte mul(UByte other) { return of((byte) (value * other.value)); }
    @Override public UByte div(UByte other) { return of((byte) ((value & MASK) / (other.value & MASK))); }
    @Override public UByte mod(UByte other) { return of((byte) ((value & MASK) % (other.value & MASK))); }
    @Override public UByte negate() { return of((byte) -value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull UByte other) { return Integer.compare(value & MASK, other.value & MASK); }

    // Math functions
    @Override public UByte abs() { return this; }
    @Override public UByte floor() { return this; }
    @Override public UByte sqrt() { return of((byte) Math.sqrt(value & MASK)); }

    // Bitwise operations
    @Override public UByte and(UByte other) { return of((byte) (value & other.value));}
    @Override public UByte or(UByte other) { return of((byte) (value | other.value)); }
    @Override public UByte xor(UByte other) { return of((byte) (value ^ other.value)); }
    @Override public UByte not() { return of((byte) ~value); }
    @Override public UByte shl(int shift) { return of((byte) (value << shift)); }
    @Override public UByte shr(int shift) { return of((byte) ((value & MASK) >>> shift)); }

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
