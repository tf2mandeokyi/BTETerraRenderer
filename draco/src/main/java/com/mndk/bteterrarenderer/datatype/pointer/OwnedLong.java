package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;

class OwnedLong extends AbstractOwnedRawLong<Long> {
    OwnedLong(long value) { super(value); }

    @Override public DataType<Long> getType() { return DataType.int64(); }
    @Override protected long toRaw(Long value) { return value; }
    @Override protected Long fromRaw(long raw) { return raw; }
    @Override public Pointer<Long> toLong() { return this; }
}
