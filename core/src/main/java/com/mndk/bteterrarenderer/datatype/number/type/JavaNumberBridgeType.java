package com.mndk.bteterrarenderer.datatype.number.type;

import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;

public abstract class JavaNumberBridgeType<T extends Number & Comparable<T>, TArray>
        implements DataNumberType<T, TArray> {
    // Number properties
    @Override public final boolean isSigned() { return true; }

    // General conversions
    @Override public final boolean equals(T left, T right) { return left.equals(right); }
    @Override public final int hashCode(T value) { return value.hashCode(); }
    @Override public final String toString(T value) { return value.toString(); }

    // Comparison operations
    @Override public final int compareTo(T left, T right) { return left.compareTo(right); }

    // Number conversions (outgoing)
    @Override public final byte toByte(T value) { return (byte) value.intValue(); }
    @Override public final short toShort(T value) { return (short) value.intValue(); }
    @Override public final int toInt(T value) { return value.intValue(); }
    @Override public final long toLong(T value) { return value.longValue(); }
    @Override public final float toFloat(T value) { return value.floatValue(); }
    @Override public final double toDouble(T value) { return value.doubleValue(); }
    @Override public final UByte toUByte(T value) { return UByte.of((byte) value.intValue()); }
    @Override public final UShort toUShort(T value) { return UShort.of((short) value.intValue()); }
    @Override public final UInt toUInt(T value) { return UInt.of(value.intValue()); }
    @Override public final ULong toULong(T value) { return ULong.of(value.longValue()); }
}
