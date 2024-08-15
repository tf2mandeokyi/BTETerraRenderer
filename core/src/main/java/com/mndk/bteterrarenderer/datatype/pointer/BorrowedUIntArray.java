package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;


public class BorrowedUIntArray extends AbstractBorrowedRawIntArray<UInt> {
    BorrowedUIntArray(int[] array, int offset) { super(array, offset); }

    @Override public DataType<UInt> getType() { return DataType.uint32(); }
    @Override public Pointer<UInt> add(int offset) { return new BorrowedUIntArray(array, this.offset + offset); }

    @Override protected int toRaw(UInt value) { return value.intValue(); }
    @Override protected UInt fromRaw(int raw) { return UInt.of(raw); }
    @Override public Pointer<UInt> toUInt() { return this; }
}
