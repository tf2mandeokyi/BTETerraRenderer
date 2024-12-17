package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UInt extends CppNumber<UInt> {

    private static final UInt[] CACHE = new UInt[256];
    static { for (int i = 0; i < 256; ++i) CACHE[i] = new UInt(i); }

    public static final UInt MIN = of(0);
    public static final UInt MAX = of(-1);
    private static final long MASK = 0xFFFFFFFFL;
    public static final UInt ZERO = of(0);

    public static UInt of(long value) { return of((int) value);}
    public static UInt of(int value) { return (value & 0xFF) == value ? CACHE[value] : new UInt(value); }
    public static UInt min(UInt a, UInt b) { return a.compareTo(b) <= 0 ? a : b; }
    public static UInt max(UInt a, UInt b) { return a.compareTo(b) >= 0 ? a : b; }

    private final int value;

    @Override public boolean equals(UInt other) { return value == other.value; }
    @Override public int hashCode() { return Integer.hashCode(value); }
    @Override public String toString() { return Integer.toUnsignedString(value); }
    @Override public String toHexString() { return Integer.toHexString(value); }
    @Override public DataNumberType<UInt> getType() { return DataType.uint32(); }

    // Arithmetic operations
    @Override public UInt add(UInt other) { return of(value + other.value); }
    @Override public UInt sub(UInt other) { return of(value - other.value); }
    @Override public UInt mul(UInt other) { return of(value * other.value); }
    @Override public UInt div(UInt other) { return of(Integer.divideUnsigned(value, other.value)); }
    @Override public UInt mod(UInt other) { return of(Integer.remainderUnsigned(value, other.value)); }
    @Override public UInt negate() { return of(-value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull UInt other) { return Integer.compareUnsigned(value, other.value); }

    // Math functions
    @Override public UInt abs() { return this; }
    @Override public UInt floor() { return this; }
    @Override public UInt sqrt() { return of((int) Math.sqrt(value & MASK)); }

    // Bitwise operations
    @Override public UInt and(UInt other) { return of(value & other.value);}
    @Override public UInt or(UInt other) { return of(value | other.value); }
    @Override public UInt xor(UInt other) { return of(value ^ other.value); }
    @Override public UInt not() { return of(~value); }
    @Override public UInt shl(int shift) { return of(value << shift); }
    @Override public UInt shr(int shift) { return of(value >>> shift); }

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
