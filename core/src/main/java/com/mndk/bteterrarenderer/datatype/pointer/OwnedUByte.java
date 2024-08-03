package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;

class OwnedUByte extends AbstractOwnedRawByte<UByte> {
    OwnedUByte(byte value) { super(value); }
    OwnedUByte(UByte value) { super(value.byteValue()); }

    @Override public DataType<UByte> getType() { return DataType.uint8(); }
    @Override protected byte toRaw(UByte value) { return value.byteValue(); }
    @Override protected UByte fromRaw(byte raw) { return UByte.of(raw); }
    @Override public Pointer<UByte> toUByte() { return this; }
}
