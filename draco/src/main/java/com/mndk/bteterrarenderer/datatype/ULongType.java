package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class ULongType extends PredefinedDataNumberType<ULong> {
    private final byte id;

    // Java overrides
    @Override public String toString() { return "uint64"; }
    @Override public boolean equals(Object obj) { return obj instanceof ULongType; }
    @Override public int hashCode() { return ULongType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 8; }
    @Override public ULong read(RawPointer src) { return src.getRawULong(); }
    @Override public void write(RawPointer dst, ULong value) { dst.setRawLong(value); }

    // General conversions
    @Override public ULong parse(String value) { return ULong.of(Long.parseUnsignedLong(value)); }
    @Override public ULong defaultValue() { return ULong.ZERO; }

    // Pointer operations
    @Override public Pointer<ULong> newOwned(ULong value) { return Pointer.newULong(value); }
    @Override public Pointer<ULong> newArray(int length) { return Pointer.newULongArray(length); }
    @Override public Pointer<ULong> castPointer(RawPointer pointer) { return pointer.toULong(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public ULong lowest() { return ULong.MIN; }
    @Override public ULong min() { return ULong.MIN; }
    @Override public ULong max() { return ULong.MAX; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return DataType.int64(); }
    @Override public DataNumberType<?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> ULong from(DataNumberType<U> type, U value) { return type.toULong(value); }
    @Override public ULong from(int value) { return ULong.of(value); }
    @Override public ULong from(long value) { return ULong.of(value); }
    @Override public ULong from(float value) { return ULong.of((long) value); }
    @Override public ULong from(double value) { return ULong.of((long) value); }
}
