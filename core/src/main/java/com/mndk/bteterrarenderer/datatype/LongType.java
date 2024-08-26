package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class LongType extends JavaNumberBridgeType<Long> {
    private final byte id;

    // Java overrides
    @Override public String toString() { return "int64"; }
    @Override public boolean equals(Object obj) { return obj instanceof LongType; }
    @Override public int hashCode() { return LongType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 8; }
    @Override public Long read(RawPointer src) { return src.getRawLong(); }
    @Override public void write(RawPointer dst, Long value) { dst.setRawLong(value); }

    // General conversions
    @Override public Long parse(String value) { return Long.parseLong(value); }
    @Override public Long defaultValue() { return 0L; }
    @Override public String toHexString(Long value) { return Long.toHexString(value); }

    // Pointer operations
    @Override public Pointer<Long> newOwned(Long value) { return Pointer.newLong(value); }
    @Override public Pointer<Long> newArray(int length) { return Pointer.newLongArray(length); }
    @Override public Pointer<Long> castPointer(RawPointer pointer) { return pointer.toLong(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public Long lowest() { return Long.MIN_VALUE; }
    @Override public Long min() { return Long.MIN_VALUE;}
    @Override public Long max() { return Long.MAX_VALUE; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return this; }
    @Override public DataNumberType<?> getUnsigned() { return DataType.uint64(); }

    // Arithmetic operations
    @Override public Long add(Long left, Long right) { return left + right; }
    @Override public Long sub(Long left, Long right) { return left - right; }
    @Override public Long mul(Long left, Long right) { return left * right; }
    @Override public Long div(Long left, Long right) { return left / right; }
    @Override public Long mod(Long left, Long right) { return left % right; }
    @Override public Long negate(Long value) { return -value; }

    // Comparison operations
    @Override public boolean lt(Long left, Long right) { return left <  right; }
    @Override public boolean le(Long left, Long right) { return left <= right; }
    @Override public boolean gt(Long left, Long right) { return left >  right; }
    @Override public boolean ge(Long left, Long right) { return left >= right; }

    // Math functions
    @Override public Long abs(Long value) { return Math.abs(value); }
    @Override public Long floor(Long value) { return value; }
    @Override public Long sqrt(Long value) { return (long) Math.sqrt(value); }

    // Bitwise operations
    @Override public Long and(Long left, Long right) { return left & right; }
    @Override public Long or(Long left, Long right) { return left | right; }
    @Override public Long xor(Long left, Long right) { return left ^ right; }
    @Override public Long not(Long value) { return ~value; }
    @Override public Long shl(Long value, int shift) { return value << shift; }
    @Override public Long shr(Long value, int shift) { return value >> shift; }

    // Number conversions (incoming)
    @Override public <U> Long from(DataNumberType<U> type, U value) { return type.toLong(value); }
    @Override public Long from(int value) { return (long) value; }
    @Override public Long from(long value) { return value; }
    @Override public Long from(float value) { return (long) value; }
    @Override public Long from(double value) { return (long) value; }

    // Number conversions (outgoing)
    @Override public boolean toBoolean(Long value) { return value != 0; }
}
