package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.*;

public abstract class PredefinedDataNumberType<T extends CppNumber<T>, TArray> implements DataNumberType<T, TArray> {
    // General conversions
    @Override public final boolean equals(T left, T right) { return left.equals(right); }
    @Override public final int hashCode(T value) { return value.hashCode(); }
    @Override public final String toString(T value) { return value.toString(); }
    @Override public final String toHexString(T value) { return value.toHexString(); }

    // Arithmetic operations
    @Override public final T add(T left, T right) { return left.add(right); }
    @Override public final T sub(T left, T right) { return left.sub(right); }
    @Override public final T mul(T left, T right) { return left.mul(right); }
    @Override public final T div(T left, T right) { return left.div(right); }
    @Override public final T mod(T left, T right) { return left.mod(right); }
    @Override public final T negate(T value) { return value.negate(); }

    // Comparison operations
    @Override public final int compareTo(T left, T right) { return left.compareTo(right); }
    @Override public final boolean lt(T left, T right) { return left.lt(right); }
    @Override public final boolean le(T left, T right) { return left.le(right); }
    @Override public final boolean gt(T left, T right) { return left.gt(right); }
    @Override public final boolean ge(T left, T right) { return left.ge(right); }

    // Math functions
    @Override public final T abs(T value) { return value.abs(); }
    @Override public final T floor(T value) { return value.floor(); }
    @Override public final T sqrt(T value) { return value.sqrt(); }

    // Bitwise operations
    @Override public final T and(T left, T right) { return left.and(right); }
    @Override public final T or(T left, T right) { return left.or(right); }
    @Override public final T xor(T left, T right) { return left.xor(right); }
    @Override public final T not(T value) { return value.not(); }
    @Override public final T shl(T value, int shift) { return value.shl(shift); }
    @Override public final T shr(T value, int shift) { return value.shr(shift); }

    // Number conversions (outgoing)
    @Override public final boolean toBoolean(T value) { return value.booleanValue(); }
    @Override public final byte toByte(T value) { return value.byteValue(); }
    @Override public final short toShort(T value) { return value.shortValue(); }
    @Override public final int toInt(T value) { return value.intValue(); }
    @Override public final long toLong(T value) { return value.longValue(); }
    @Override public final float toFloat(T value) { return value.floatValue(); }
    @Override public final double toDouble(T value) { return value.doubleValue(); }
    @Override public final UByte toUByte(T value) { return value.uByteValue(); }
    @Override public final UShort toUShort(T value) { return value.uShortValue(); }
    @Override public final UInt toUInt(T value) { return value.uIntValue(); }
    @Override public final ULong toULong(T value) { return value.uLongValue(); }
}
