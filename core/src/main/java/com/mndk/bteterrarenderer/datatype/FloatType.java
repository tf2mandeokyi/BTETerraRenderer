package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class FloatType extends JavaNumberBridgeType<Float> {
    // Java overrides
    @Override public String toString() { return "float32"; }
    @Override public boolean equals(Object obj) { return obj instanceof FloatType; }
    @Override public int hashCode() { return FloatType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 4; }
    @Override public Float read(RawPointer src) { return DataType.fromRaw(src.getRawInt()); }
    @Override public void write(RawPointer dst, Float value) {
        dst.setRawInt(DataType.toRaw(value));
    }

    // General conversions
    @Override public Float parse(String value) { return Float.parseFloat(value); }
    @Override public Float defaultValue() { return 0f; }
    @Override public String toHexString(Float value) { return Float.toHexString(value); }

    // Pointer operations
    @Override public Pointer<Float> newOwned(Float value) { return Pointer.newFloat(value); }
    @Override public Pointer<Float> newArray(int length) { return Pointer.newFloatArray(length); }
    @Override public Pointer<Float> castPointer(RawPointer pointer) { return pointer.toFloat(); }

    // Number properties
    @Override public boolean isIntegral() { return false; }
    @Override public Float lowest() { return -Float.MAX_VALUE; }
    @Override public Float min() { return Float.MIN_VALUE;}
    @Override public Float max() { return Float.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return this; }
    @Override public DataNumberType<?> getUnsigned() { throw new UnsupportedOperationException(); }

    // Arithmetic operations
    @Override public Float add(Float left, Float right) { return left + right; }
    @Override public Float sub(Float left, Float right) { return left - right; }
    @Override public Float mul(Float left, Float right) { return left * right; }
    @Override public Float div(Float left, Float right) { return left / right; }
    @Override public Float mod(Float left, Float right) { return left % right; }
    @Override public Float negate(Float value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Float left, Float right) { return left <  right; }
    @Override public boolean le(Float left, Float right) { return left <= right; }
    @Override public boolean gt(Float left, Float right) { return left >  right; }
    @Override public boolean ge(Float left, Float right) { return left >= right; }

    // Math functions
    @Override public Float abs(Float value) { return Math.abs(value); }
    @Override public Float floor(Float value) { return (float) Math.floor(value); }
    @Override public Float sqrt(Float value) { return (float) Math.sqrt(value); }

    // Bitwise operations
    @Override public Float and(Float left, Float right) { throw new UnsupportedOperationException(); }
    @Override public Float or(Float left, Float right) { throw new UnsupportedOperationException(); }
    @Override public Float xor(Float left, Float right) { throw new UnsupportedOperationException(); }
    @Override public Float not(Float value) { throw new UnsupportedOperationException(); }
    @Override public Float shl(Float value, int shift) { throw new UnsupportedOperationException(); }
    @Override public Float shr(Float value, int shift) { throw new UnsupportedOperationException(); }

    // Number conversions (incoming)
    @Override public <U> Float from(DataCalculator<U> type, U value) { return type.toFloat(value); }
    @Override public Float from(int value) { return (float) value; }
    @Override public Float from(long value) { return (float) value; }
    @Override public Float from(float value) { return value; }
    @Override public Float from(double value) { return (float) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Float value) { return value != 0; }
}
