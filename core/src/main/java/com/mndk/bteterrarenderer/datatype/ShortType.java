package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class ShortType extends JavaNumberBridgeType<Short> {
    // Java overrides
    @Override public String toString() { return "int16"; }
    @Override public boolean equals(Object obj) { return obj instanceof ShortType; }
    @Override public int hashCode() { return ShortType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 2; }
    @Override public Short read(RawPointer src) { return src.getRawShort(); }
    @Override public void write(RawPointer dst, Short value) { dst.setRawShort(value); }

    // General conversions
    @Override public Short parse(String value) { return Short.parseShort(value); }
    @Override public Short defaultValue() { return 0; }
    @Override public String toHexString(Short value) { return Integer.toHexString(value & 0xFFFF); }

    // Pointer operations
    @Override public Pointer<Short> newOwned(Short value) { return Pointer.newShort(value); }
    @Override public Pointer<Short> newArray(int length) { return Pointer.wrap(new short[length], 0); }
    @Override public Pointer<Short> castPointer(RawPointer pointer) { return pointer.toShort(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Short lowest() { return Short.MIN_VALUE; }
    @Override public Short min() { return Short.MIN_VALUE;}
    @Override public Short max() { return Short.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return this; }
    @Override public DataNumberType<?> getUnsigned() { return DataType.uint16(); }

    // Arithmetic operations
    @Override public Short add(Short left, Short right) { return (short) (left + right); }
    @Override public Short sub(Short left, Short right) { return (short) (left - right); }
    @Override public Short mul(Short left, Short right) { return (short) (left * right); }
    @Override public Short div(Short left, Short right) { return (short) (left / right); }
    @Override public Short mod(Short left, Short right) { return (short) (left % right); }
    @Override public Short negate(Short value) { return (short) -value; }

    // Comparison operations
    @Override public boolean lt(Short left, Short right) { return left <  right; }
    @Override public boolean le(Short left, Short right) { return left <= right; }
    @Override public boolean gt(Short left, Short right) { return left >  right; }
    @Override public boolean ge(Short left, Short right) { return left >= right; }

    // Math functions
    @Override public Short abs(Short value) { return (short) Math.abs(value); }
    @Override public Short floor(Short value) { return value; }
    @Override public Short sqrt(Short value) { return (short) Math.sqrt(value); }

    // Bitwise operations
    @Override public Short and(Short left, Short right) { return (short) (left & right); }
    @Override public Short or(Short left, Short right) { return (short) (left | right); }
    @Override public Short xor(Short left, Short right) { return (short) (left ^ right); }
    @Override public Short not(Short value) { return (short) ~value; }
    @Override public Short shl(Short value, int shift) { return (short) (value << shift); }
    @Override public Short shr(Short value, int shift) { return (short) (value >> shift); }

    // Number conversions (incoming)
    @Override public <U> Short from(DataCalculator<U> type, U value) { return type.toShort(value); }
    @Override public Short from(int value) { return (short) value; }
    @Override public Short from(long value) { return (short) value; }
    @Override public Short from(float value) { return (short) value; }
    @Override public Short from(double value) { return (short) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Short value) { return value != 0; }
}
