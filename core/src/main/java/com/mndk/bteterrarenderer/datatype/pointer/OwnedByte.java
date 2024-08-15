package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;

class OwnedByte extends AbstractOwnedRawByte<Byte> {
    public OwnedByte(byte value) { super(value); }

    @Override public DataType<Byte> getType() { return DataType.int8(); }
    @Override public byte toRaw(Byte value) { return value; }
    @Override public Byte fromRaw(byte raw) { return raw; }
    @Override public Pointer<Byte> toByte() { return this; }
}
