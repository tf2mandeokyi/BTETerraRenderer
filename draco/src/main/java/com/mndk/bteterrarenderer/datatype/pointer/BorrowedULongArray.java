package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.ULong;


public class BorrowedULongArray extends AbstractBorrowedRawLongArray<ULong> {
    BorrowedULongArray(long[] array, int offset) { super(array, offset); }

    @Override public DataType<ULong> getType() { return DataType.uint64(); }
    @Override public Pointer<ULong> add(int offset) { return new BorrowedULongArray(array, this.offset + offset); }

    @Override protected long toRaw(ULong value) { return value.longValue(); }
    @Override protected ULong fromRaw(long raw) { return ULong.of(raw); }
    @Override public Pointer<ULong> toULong() { return this; }
}
