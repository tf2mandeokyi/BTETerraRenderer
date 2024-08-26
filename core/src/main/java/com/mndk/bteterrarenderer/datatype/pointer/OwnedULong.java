package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.ULong;

class OwnedULong extends AbstractOwnedRawLong<ULong> {
    OwnedULong(long value) { super(value); }
    OwnedULong(ULong value) { super(value.longValue()); }

    @Override public DataType<ULong> getType() { return DataType.uint64(); }
    @Override protected long toRaw(ULong value) { return value.longValue(); }
    @Override protected ULong fromRaw(long raw) { return ULong.of(raw); }
    @Override public Pointer<ULong> toULong() { return this; }
}
