package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;

class BorrowedShortArray extends AbstractBorrowedRawShortArray<Short> {
    BorrowedShortArray(short[] array, int offset) { super(array, offset); }

    @Override public DataType<Short> getType() { return DataType.int16(); }
    @Override public Pointer<Short> add(int offset) { return new BorrowedShortArray(array, this.offset + offset); }

    @Override protected short toRaw(Short value) { return value; }
    @Override protected Short fromRaw(short raw) { return raw; }
    @Override public Pointer<Short> toShort() { return this; }
}
