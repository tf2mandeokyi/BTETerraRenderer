package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UShort;

class OwnedUShort extends AbstractOwnedRawShort<UShort> {
    OwnedUShort(short value) { super(value); }
    OwnedUShort(UShort value) { super(value.shortValue()); }

    @Override public DataType<UShort> getType() { return DataType.uint16(); }
    @Override protected short toRaw(UShort value) { return value.shortValue(); }
    @Override protected UShort fromRaw(short raw) { return UShort.of(raw); }
    @Override public Pointer<UShort> toUShort() { return this; }
}
