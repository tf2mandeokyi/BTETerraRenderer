package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;

class BorrowedLongArray extends AbstractBorrowedRawLongArray<Long> {
    BorrowedLongArray(long[] array, int offset) { super(array, offset); }

    @Override public DataType<Long> getType() { return DataType.int64(); }
    @Override public Pointer<Long> add(int offset) { return new BorrowedLongArray(array, this.offset + offset); }

    @Override protected long toRaw(Long value) { return value; }
    @Override protected Long fromRaw(long raw) { return raw; }
    @Override public Pointer<Long> toLong() { return this; }
}
