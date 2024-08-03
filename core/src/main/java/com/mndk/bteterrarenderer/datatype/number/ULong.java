package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ULong extends CppNumber<ULong> {
    public static final ULong MIN = new ULong(0);
    public static final ULong MAX = new ULong(-1);
    public static final ULong ZERO = new ULong(0);
    private static final float POW_63F = (float) Math.pow(2, 63);
    private static final double POW_63D = Math.pow(2, 63);

    public static ULong of(long value) { return new ULong(value); }
    public static ULong[] array(Long... values) { return Stream.of(values).map(ULong::of).toArray(ULong[]::new); }
    public static List<ULong> list(Long... values) { return Stream.of(values).map(ULong::of).collect(Collectors.toList()); }

    private final long value;

    @Override public boolean equals(ULong other) { return value == other.value; }
    @Override public int hashCode() { return Long.hashCode(value); }
    @Override public String toString() { return Long.toUnsignedString(value); }
    @Override public String toHexString() { return Long.toHexString(value); }
    @Override public DataNumberType<ULong> getType() { return DataType.uint64(); }

    // Arithmetic operations
    @Override public ULong add(ULong other) { return new ULong(value + other.value); }
    @Override public ULong sub(ULong other) { return new ULong(value - other.value); }
    @Override public ULong mul(ULong other) { return new ULong(value * other.value); }
    @Override public ULong div(ULong other) { return new ULong(Long.divideUnsigned(value, other.value)); }
    @Override public ULong mod(ULong other) { return new ULong(Long.remainderUnsigned(value, other.value)); }
    @Override public ULong negate() { return new ULong(-value); }

    // Comparison operations
    @Override public int compareTo(@Nonnull ULong other) { return Long.compareUnsigned(value, other.value); }

    // Math functions
    @Override public ULong abs() { return this; }
    @Override public ULong floor() { return this; }
    @Override public ULong sqrt() { return new ULong((long) Math.sqrt(this.doubleValue())); }

    // Bitwise operations
    @Override public ULong and(ULong other) { return new ULong(value & other.value);}
    @Override public ULong or(ULong other) { return new ULong(value | other.value); }
    @Override public ULong xor(ULong other) { return new ULong(value ^ other.value); }
    @Override public ULong not() { return new ULong(~value); }
    @Override public ULong shl(int shift) { return new ULong(value << shift); }
    @Override public ULong shr(int shift) { return new ULong(value >>> shift); }

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
