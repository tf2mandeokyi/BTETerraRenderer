package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class UByteType extends PredefinedDataNumberType<UByte> {
    // Java overrides
    @Override public String toString() { return "uint8_t"; }
    @Override public boolean equals(Object obj) { return obj instanceof UByteType; }
    @Override public int hashCode() { return UByteType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 1; }
    @Override public UByte read(RawPointer src) { return src.getRawUByte(); }
    @Override public void write(RawPointer dst, UByte value) { dst.setRawByte(value); }

    // General conversions
    @Override public UByte parse(String value) { return UByte.of((byte) Short.parseShort(value)); }
    @Override public UByte defaultValue() { return UByte.ZERO; }

    // Pointer operations
    @Override public Pointer<UByte> newOwned(UByte value) { return Pointer.newUByte(value); }
    @Override public Pointer<UByte> newArray(int length) { return Pointer.wrapUnsigned(new byte[length], 0); }
    @Override public Pointer<UByte> castPointer(RawPointer pointer) { return pointer.toUByte(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public UByte lowest() { return UByte.MIN; }
    @Override public UByte min() { return UByte.MIN;}
    @Override public UByte max() { return UByte.MAX; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return DataType.int8(); }
    @Override public DataNumberType<?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> UByte from(DataCalculator<U> type, U value) { return type.toUByte(value); }
    @Override public UByte from(int value) { return UByte.of(value); }
    @Override public UByte from(long value) { return UByte.of((int) value); }
    @Override public UByte from(float value) { return UByte.of((int) value); }
    @Override public UByte from(double value) { return UByte.of((int) value); }
}
