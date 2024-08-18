package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class UIntType extends PredefinedDataNumberType<UInt> {
    // Java overrides
    @Override public String toString() { return "uint32"; }
    @Override public boolean equals(Object obj) { return obj instanceof UIntType; }
    @Override public int hashCode() { return UIntType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 4; }
    @Override public UInt read(RawPointer src) { return src.getRawUInt(); }
    @Override public void write(RawPointer dst, UInt value) { dst.setRawInt(value); }

    // General conversions
    @Override public UInt parse(String value) { return UInt.of(Integer.parseUnsignedInt(value)); }
    @Override public UInt defaultValue() { return UInt.ZERO; }

    @Override public Pointer<UInt> newOwned(UInt value) { return Pointer.newUInt(value); }
    @Override public Pointer<UInt> newArray(int length) { return Pointer.newUIntArray(length); }
    @Override public Pointer<UInt> castPointer(RawPointer pointer) { return pointer.toUInt(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public UInt lowest() { return UInt.MIN; }
    @Override public UInt min() { return UInt.MIN; }
    @Override public UInt max() { return UInt.MAX; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return DataType.int32(); }
    @Override public DataNumberType<?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> UInt from(DataCalculator<U> type, U value) { return type.toUInt(value); }
    @Override public UInt from(int value) { return UInt.of(value); }
    @Override public UInt from(long value) { return UInt.of((int) value); }
    @Override public UInt from(float value) { return UInt.of((int) value); }
    @Override public UInt from(double value) { return UInt.of((int) value); }
}
