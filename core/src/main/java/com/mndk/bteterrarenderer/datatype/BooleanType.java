package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.*;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class BooleanType implements DataNumberType<Boolean> {
    // Java overrides
    @Override public String toString() { return "bool"; }
    @Override public boolean equals(Object obj) { return obj instanceof BooleanType; }
    @Override public int hashCode() { return BooleanType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 1; }
    @Override public Boolean read(RawPointer src) { return DataType.fromRaw(src.getRawByte()); }
    @Override public void write(RawPointer dst, Boolean value) { dst.setRawByte(DataType.toRaw(value)); }

    // General conversions
    @Override public Boolean parse(String value) { return !value.equals("0"); }
    @Override public Boolean defaultValue() { return false; }
    @Override public boolean equals(Boolean left, Boolean right) { return left.equals(right); }
    @Override public int hashCode(Boolean value) { return Boolean.hashCode(value); }
    @Override public String toString(Boolean value) { return value ? "1" : "0"; }
    @Override public String toHexString(Boolean value) { return value ? "1" : "0"; }

    // Pointer operations
    @Override public Pointer<Boolean> newOwned(Boolean value) { return Pointer.newBool(value); }
    @Override public Pointer<Boolean> newArray(int length) { return Pointer.newBoolArray(length); }
    @Override public Pointer<Boolean> castPointer(RawPointer pointer) { return pointer.toBool(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public Boolean lowest() { return false; }
    @Override public Boolean min() { return false; }
    @Override public Boolean max() { return true; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { throw new UnsupportedOperationException(); }
    @Override public DataNumberType<?> getUnsigned() { return this; }

    // Arithmetic operations: Based on the C++ behavior of bool
    @Override public Boolean add(Boolean left, Boolean right) { return left || right; }
    @Override public Boolean sub(Boolean left, Boolean right) { return left ^ right; }
    @Override public Boolean mul(Boolean left, Boolean right) { return left && right; }
    @Override public Boolean div(Boolean left, Boolean right) {
        if(!right) throw new ArithmeticException("Division by zero");
        return left;
    }
    @Override public Boolean mod(Boolean left, Boolean right) {
        if(!right) throw new ArithmeticException("Division by zero");
        return false;
    }
    @Override public Boolean negate(Boolean value) { return value; }

    // Comparison operations
    @Override public int compareTo(Boolean left, Boolean right) { return left.compareTo(right); }
    @Override public boolean lt(Boolean left, Boolean right) { return !left &&  right; }
    @Override public boolean le(Boolean left, Boolean right) { return !left ||  right; }
    @Override public boolean gt(Boolean left, Boolean right) { return  left && !right; }
    @Override public boolean ge(Boolean left, Boolean right) { return  left || !right; }

    // Math functions
    @Override public Boolean abs(Boolean value) { return value; }
    @Override public Boolean floor(Boolean value) { return value; }
    @Override public Boolean sqrt(Boolean value) { return value; }

    // Bitwise operations
    @Override public Boolean and(Boolean left, Boolean right) { return left && right; }
    @Override public Boolean or(Boolean left, Boolean right) { return left || right; }
    @Override public Boolean xor(Boolean left, Boolean right) { return left ^ right; }
    @Override public Boolean not(Boolean value) { return !value; }
    @Override public Boolean shl(Boolean value, int shift) { return value; }
    @Override public Boolean shr(Boolean value, int shift) { return value && shift == 0; }

    // Number conversions (incoming)
    @Override public <U> Boolean from(DataCalculator<U> type, U value) { return type.toBoolean(value); }
    @Override public Boolean from(int value) { return value != 0; }
    @Override public Boolean from(long value) { return value != 0; }
    @Override public Boolean from(float value) { return value != 0; }
    @Override public Boolean from(double value) { return value != 0; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Boolean value) { return value; }
    @Override public byte toByte(Boolean value) { return (byte) (value ? 1 : 0); }
    @Override public short toShort(Boolean value) { return (short) (value ? 1 : 0); }
    @Override public int toInt(Boolean value) { return value ? 1 : 0; }
    @Override public long toLong(Boolean value) { return value ? 1 : 0; }
    @Override public float toFloat(Boolean value) { return value ? 1 : 0; }
    @Override public double toDouble(Boolean value) { return value ? 1 : 0; }
    @Override public UByte toUByte(Boolean value) { return UByte.of(value ? 1 : 0); }
    @Override public UShort toUShort(Boolean value) { return UShort.of(value ? 1 : 0); }
    @Override public UInt toUInt(Boolean value) { return UInt.of(value ? 1 : 0); }
    @Override public ULong toULong(Boolean value) { return ULong.of(value ? 1 : 0); }
}
