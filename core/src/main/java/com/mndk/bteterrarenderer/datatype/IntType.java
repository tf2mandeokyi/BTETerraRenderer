package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class IntType extends JavaNumberBridgeType<Integer> {
    // Java overrides
    @Override public String toString() { return "int32"; }
    @Override public boolean equals(Object obj) { return obj instanceof IntType; }
    @Override public int hashCode() { return IntType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 4; }
    @Override public Integer read(RawPointer src) { return src.getRawInt(); }
    @Override public void write(RawPointer dst, Integer value) { dst.setRawInt(value); }

    // General conversions
    @Override public Integer parse(String value) { return Integer.parseInt(value); }
    @Override public Integer defaultValue() { return 0; }
    @Override public String toHexString(Integer value) { return Integer.toHexString(value); }

    // Pointer operations
    @Override public Pointer<Integer> newOwned(Integer value) { return Pointer.newInt(value); }
    @Override public Pointer<Integer> newArray(int length) { return Pointer.wrap(new int[length], 0); }
    @Override public Pointer<Integer> castPointer(RawPointer pointer) { return pointer.toInt(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Integer lowest() { return Integer.MIN_VALUE; }
    @Override public Integer min() { return Integer.MIN_VALUE;}
    @Override public Integer max() { return Integer.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return this; }
    @Override public DataNumberType<?> getUnsigned() { return DataType.uint32(); }

    // Arithmetic operations
    @Override public Integer add(Integer left, Integer right) { return left + right; }
    @Override public Integer sub(Integer left, Integer right) { return left - right; }
    @Override public Integer mul(Integer left, Integer right) { return left * right; }
    @Override public Integer div(Integer left, Integer right) { return left / right; }
    @Override public Integer mod(Integer left, Integer right) { return left % right; }
    @Override public Integer negate(Integer value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Integer left, Integer right) { return left <  right; }
    @Override public boolean le(Integer left, Integer right) { return left <= right; }
    @Override public boolean gt(Integer left, Integer right) { return left >  right; }
    @Override public boolean ge(Integer left, Integer right) { return left >= right; }

    // Math functions
    @Override public Integer abs(Integer value) { return Math.abs(value); }
    @Override public Integer floor(Integer value) { return value; }
    @Override public Integer sqrt(Integer value) { return (int) Math.sqrt(value); }

    // Bitwise operations
    @Override public Integer and(Integer left, Integer right) { return left & right; }
    @Override public Integer or(Integer left, Integer right) { return left | right; }
    @Override public Integer xor(Integer left, Integer right) { return left ^ right; }
    @Override public Integer not(Integer value) { return ~value; }
    @Override public Integer shl(Integer value, int shift) { return value << shift; }
    @Override public Integer shr(Integer value, int shift) { return value >> shift; }

    // Number conversions (incoming)
    @Override public <U> Integer from(DataCalculator<U> type, U value) { return type.toInt(value); }
    @Override public Integer from(int value) { return value; }
    @Override public Integer from(long value) { return (int) value; }
    @Override public Integer from(float value) { return (int) value; }
    @Override public Integer from(double value) { return (int) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Integer value) { return value != 0; }
}
