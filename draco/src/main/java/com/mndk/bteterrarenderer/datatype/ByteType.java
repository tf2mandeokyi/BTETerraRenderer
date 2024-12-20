package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class ByteType extends JavaNumberBridgeType<Byte> {
    private final byte id;

    // Java overrides
    @Override public String toString() { return "int8"; }
    @Override public boolean equals(Object obj) { return obj instanceof ByteType; }
    @Override public int hashCode() { return ByteType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 1; }
    @Override public Byte read(RawPointer src) { return src.getRawByte(); }
    @Override public void write(RawPointer dst, Byte value) { dst.setRawByte(value); }

    // General conversions
    @Override public Byte parse(String value) { return Byte.parseByte(value); }
    @Override public Byte defaultValue() { return 0; }
    @Override public String toHexString(Byte value) { return Integer.toHexString(value & 0xFF); }

    // Pointer operations
    @Override public Pointer<Byte> newOwned(Byte value) { return Pointer.newByte(value); }
    @Override public Pointer<Byte> newArray(int length) { return Pointer.newByteArray(length); }
    @Override public Pointer<Byte> castPointer(RawPointer pointer) { return pointer.toByte(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Byte lowest() { return Byte.MIN_VALUE; }
    @Override public Byte min() { return Byte.MIN_VALUE;}
    @Override public Byte max() { return Byte.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return this; }
    @Override public DataNumberType<?> getUnsigned() { return DataType.uint8(); }

    // Arithmetic operations
    @Override public Byte add(Byte left, Byte right) { return (byte) (left + right); }
    @Override public Byte sub(Byte left, Byte right) { return (byte) (left - right); }
    @Override public Byte mul(Byte left, Byte right) { return (byte) (left * right); }
    @Override public Byte div(Byte left, Byte right) { return (byte) (left / right); }
    @Override public Byte mod(Byte left, Byte right) { return (byte) (left % right); }
    @Override public Byte negate(Byte value) { return (byte) -value; }

    // Comparison operations
    @Override public boolean lt(Byte left, Byte right) { return left <  right; }
    @Override public boolean le(Byte left, Byte right) { return left <= right; }
    @Override public boolean gt(Byte left, Byte right) { return left >  right; }
    @Override public boolean ge(Byte left, Byte right) { return left >= right; }

    // Math functions
    @Override public Byte abs(Byte value) { return (byte) Math.abs(value); }
    @Override public Byte floor(Byte value) { return value; }
    @Override public Byte sqrt(Byte value) { return (byte) Math.sqrt(value); }

    // Bitwise operations
    @Override public Byte and(Byte left, Byte right) { return (byte) (left & right); }
    @Override public Byte or(Byte left, Byte right) { return (byte) (left | right); }
    @Override public Byte xor(Byte left, Byte right) { return (byte) (left ^ right); }
    @Override public Byte not(Byte value) { return (byte) ~value; }
    @Override public Byte shl(Byte value, int shift) { return (byte) (value << shift); }
    @Override public Byte shr(Byte value, int shift) { return (byte) (value >> shift); }

    // Number conversions (incoming)
    @Override public <U> Byte from(DataNumberType<U> type, U value) { return type.toByte(value); }
    @Override public Byte from(int value) { return (byte) value; }
    @Override public Byte from(long value) { return (byte) value; }
    @Override public Byte from(float value) { return (byte) value; }
    @Override public Byte from(double value) { return (byte) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Byte value) { return value != 0; }
}
