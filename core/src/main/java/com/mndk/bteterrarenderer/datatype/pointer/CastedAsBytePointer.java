package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;

class CastedAsBytePointer extends CastedPointer<Byte> implements RawBytePointer {
    CastedAsBytePointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<Byte> getType() { return DataType.int8(); }
    @Override public Byte get() { return pointer.getRawByte(offset); }
    @Override public Byte get(long index) { return pointer.getRawByte(offset + index); }
    @Override public void set(Byte value) { pointer.setRawByte(offset, value); }
    @Override public void set(long index, Byte value) { pointer.setRawByte(offset + index, value); }
    @Override public Pointer<Byte> add(long offset) { return new CastedAsBytePointer(pointer, this.offset + offset); }

    @Override public byte getRawByte(long index) { return pointer.getRawByte(offset + index); }
    @Override public void setRawByte(long index, byte value) { pointer.setRawByte(offset + index, value); }
    @Override public Pointer<Boolean> toBool() { return new CastedAsBooleanPointer(pointer, offset); }
    @Override public Pointer<Byte> toByte() { return this; }
    @Override public Pointer<UByte> toUByte() { return new CastedAsUBytePointer(pointer, offset); }
}
