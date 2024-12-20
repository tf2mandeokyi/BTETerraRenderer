package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UShort;


public class BorrowedUShortArray extends AbstractBorrowedRawShortArray<UShort> {
    BorrowedUShortArray(short[] array, int offset) { super(array, offset); }

    @Override public DataType<UShort> getType() { return DataType.uint16(); }
    @Override public Pointer<UShort> add(int offset) { return new BorrowedUShortArray(array, this.offset + offset); }

    @Override protected short toRaw(UShort value) { return value.shortValue(); }
    @Override protected UShort fromRaw(short raw) { return UShort.of(raw); }
    @Override public Pointer<UShort> toUShort() { return this; }
}
