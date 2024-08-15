package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;

class BorrowedIntArray extends AbstractBorrowedRawIntArray<Integer> {
    BorrowedIntArray(int[] array, int offset) { super(array, offset); }

    @Override public DataType<Integer> getType() { return DataType.int32(); }
    @Override public Pointer<Integer> add(int offset) { return new BorrowedIntArray(array, this.offset + offset); }

    @Override protected int toRaw(Integer value) { return value; }
    @Override protected Integer fromRaw(int raw) { return raw; }
    @Override public Pointer<Integer> toInt() { return this; }
}
