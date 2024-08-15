package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class DoubleType extends JavaNumberBridgeType<Double> {
    // Java overrides
    @Override public String toString() { return "float64"; }
    @Override public boolean equals(Object obj) { return obj instanceof DoubleType; }
    @Override public int hashCode() { return DoubleType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 8; }
    @Override public Double read(RawPointer src) { return DataType.fromRaw(src.getRawLong()); }
    @Override public void write(RawPointer dst, Double value) { dst.setRawLong(DataType.toRaw(value)); }

    // General conversions
    @Override public Double parse(String value) { return Double.parseDouble(value); }
    @Override public Double defaultValue() { return 0d; }
    @Override public String toHexString(Double value) { return Double.toHexString(value); }

    // Pointer operations
    @Override public Pointer<Double> newOwned(Double value) { return Pointer.newDouble(value); }
    @Override public Pointer<Double> newArray(int length) { return Pointer.wrap(new double[length], 0); }
    @Override public Pointer<Double> castPointer(RawPointer pointer) { return pointer.toDouble(); }

    // Number properties
    @Override public boolean isIntegral() { return false; }
    @Override public Double lowest() { return -Double.MAX_VALUE; }
    @Override public Double min() { return Double.MIN_VALUE;}
    @Override public Double max() { return Double.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return this; }
    @Override public DataNumberType<?> getUnsigned() { throw new UnsupportedOperationException(); }

    // Arithmetic operations
    @Override public Double add(Double left, Double right) { return left + right; }
    @Override public Double sub(Double left, Double right) { return left - right; }
    @Override public Double mul(Double left, Double right) { return left * right; }
    @Override public Double div(Double left, Double right) { return left / right; }
    @Override public Double mod(Double left, Double right) { return left % right; }
    @Override public Double negate(Double value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Double left, Double right) { return left <  right; }
    @Override public boolean le(Double left, Double right) { return left <= right; }
    @Override public boolean gt(Double left, Double right) { return left >  right; }
    @Override public boolean ge(Double left, Double right) { return left >= right; }

    // Math functions
    @Override public Double abs(Double value) { return Math.abs(value); }
    @Override public Double floor(Double value) { return Math.floor(value); }
    @Override public Double sqrt(Double value) { return Math.sqrt(value); }

    // Bitwise operations
    @Override public Double and(Double left, Double right) { throw new UnsupportedOperationException(); }
    @Override public Double or(Double left, Double right) { throw new UnsupportedOperationException(); }
    @Override public Double xor(Double left, Double right) { throw new UnsupportedOperationException(); }
    @Override public Double not(Double value) { throw new UnsupportedOperationException(); }
    @Override public Double shl(Double value, int shift) { throw new UnsupportedOperationException(); }
    @Override public Double shr(Double value, int shift) { throw new UnsupportedOperationException(); }

    // Number conversions (incoming)
    @Override public <U> Double from(DataCalculator<U> type, U value) { return type.toDouble(value); }
    @Override public Double from(int value) { return (double) value; }
    @Override public Double from(long value) { return (double) value; }
    @Override public Double from(float value) { return (double) value; }
    @Override public Double from(double value) { return value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Double value) { return value != 0; }
}
