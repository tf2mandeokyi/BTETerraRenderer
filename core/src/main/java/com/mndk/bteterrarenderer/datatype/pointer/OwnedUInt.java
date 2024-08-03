package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;

class OwnedUInt extends AbstractOwnedRawInt<UInt> {
    OwnedUInt(int value) { super(value); }
    OwnedUInt(UInt value) { super(value.intValue()); }

    @Override public DataType<UInt> getType() { return DataType.uint32(); }
    @Override protected int toRaw(UInt value) { return value.intValue(); }
    @Override protected UInt fromRaw(int raw) { return UInt.of(raw); }
    @Override public Pointer<UInt> asRawToUInt() { return this; }
}
