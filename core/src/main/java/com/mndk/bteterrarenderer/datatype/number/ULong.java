package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ULong extends CppNumber<ULong> {

    private static final ULong[] CACHE = new ULong[256];
    static { for(int i = 0; i < 256; ++i) CACHE[i] = new ULong(i); }

    public static final ULong MIN = of(0);
    public static final ULong MAX = of(-1);
    public static final ULong ZERO = of(0);
    private static final float POW_63F = (float) Math.pow(2, 63);
    private static final double POW_63D = Math.pow(2, 63);

    public static ULong of(long value) { return 0 <= value && value < 256 ? CACHE[(int) value] : new ULong(value); }

    private final long value;

    @Override public boolean equals(ULong other) { return value == other.value; }
    @Override public int hashCode() { return Long.hashCode(value); }
    @Override public String toString() { return Long.toUnsignedString(value); }
    @Override public String toHexString() { return Long.toHexString(value); }
    @Override public DataNumberType<ULong> getType() { return DataType.uint64(); }

    // Arithmetic operations
    @Override public ULong add(ULong other) { return of(value + other.value); }
    @Override public ULong sub(ULong other) { return of(value - other.value); }
    @Override public ULong mul(ULong other) { return of(value * other.value); }
    @Override public ULong div(ULong other) { return of(Long.divideUnsigned(value, other.value)); }
    @Override public ULong mod(ULong other) { return of(Long.remainderUnsigned(value, other.value)); }
    @Override public ULong negate() { return of(-value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull ULong other) { return Long.compareUnsigned(value, other.value); }

    // Math functions
    @Override public ULong abs() { return this; }
    @Override public ULong floor() { return this; }
    @Override public ULong sqrt() { return of((long) Math.sqrt(this.doubleValue())); }

    // Bitwise operations
    @Override public ULong and(ULong other) { return of(value & other.value);}
    @Override public ULong or(ULong other) { return of(value | other.value); }
    @Override public ULong xor(ULong other) { return of(value ^ other.value); }
    @Override public ULong not() { return of(~value); }
    @Override public ULong shl(int shift) { return of(value << shift); }
    @Override public ULong shr(int shift) { return of(value >>> shift); }

    @Override public boolean booleanValue() { return value != 0; }
    @Override public byte byteValue() { return (byte) value; }
    @Override public short shortValue() { return (short) value; }
    @Override public int intValue() { return (int) this.value; }
    @Override public long longValue() { return this.value; }
    @Override public float floatValue() { return (this.value & Long.MAX_VALUE) + (this.value >>> 63) * POW_63F; }
    @Override public double doubleValue() { return (this.value & Long.MAX_VALUE) + (this.value >>> 63) * POW_63D; }
    @Override public UByte uByteValue() { return UByte.of((byte) value); }
    @Override public UShort uShortValue() { return UShort.of((short) value); }
    @Override public UInt uIntValue() { return UInt.of((int) value); }
    @Override public ULong uLongValue() { return this; }
}
