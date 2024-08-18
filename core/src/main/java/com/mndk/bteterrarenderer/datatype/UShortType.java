package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.DataCalculator;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

class UShortType extends PredefinedDataNumberType<UShort> {
    // Java overrides
    @Override public String toString() { return "uint16"; }
    @Override public boolean equals(Object obj) { return obj instanceof UShortType; }
    @Override public int hashCode() { return UShortType.class.hashCode(); }

    // IO operations
    @Override public long byteSize() { return 2; }
    @Override public UShort read(RawPointer src) { return src.getRawUShort(); }
    @Override public void write(RawPointer dst, UShort value) { dst.setRawShort(value); }

    // General conversions
    @Override public UShort parse(String value) { return UShort.of((short) Integer.parseInt(value)); }
    @Override public UShort defaultValue() { return UShort.ZERO; }

    // Pointer operations
    @Override public Pointer<UShort> newOwned(UShort value) { return Pointer.newUShort(value); }
    @Override public Pointer<UShort> newArray(int length) { return Pointer.newUShortArray(length); }
    @Override public Pointer<UShort> castPointer(RawPointer pointer) { return pointer.toUShort(); }

    // Number properties
    @Override public boolean isIntegral() { return true; }
    @Override public boolean isSigned() { return false; }
    @Override public UShort lowest() { return UShort.MIN; }
    @Override public UShort min() { return UShort.MIN; }
    @Override public UShort max() { return UShort.MAX; }

    // Type conversions
    @Override public DataNumberType<?> getSigned() { return DataType.int16(); }
    @Override public DataNumberType<?> getUnsigned() { return this; }

    // Number conversions (incoming)
    @Override public <U> UShort from(DataCalculator<U> type, U value) { return type.toUShort(value); }
    @Override public UShort from(int value) { return UShort.of(value); }
    @Override public UShort from(long value) { return UShort.of((int) value); }
    @Override public UShort from(float value) { return UShort.of((int) value); }
    @Override public UShort from(double value) { return UShort.of((int) value); }
}
