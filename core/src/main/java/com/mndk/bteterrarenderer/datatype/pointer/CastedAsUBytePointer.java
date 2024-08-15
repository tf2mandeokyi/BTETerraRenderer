package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;

class CastedAsUBytePointer extends CastedPointer<UByte> implements RawBytePointer {
    CastedAsUBytePointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<UByte> getType() { return DataType.uint8(); }
    @Override public UByte get() { return UByte.of(pointer.getRawByte(offset)); }
    @Override public UByte get(long index) { return UByte.of(pointer.getRawByte(offset + index)); }
    @Override public void set(UByte value) { pointer.setRawByte(offset, value); }
    @Override public void set(long index, UByte value) { pointer.setRawByte(offset + index, value); }
    @Override public Pointer<UByte> add(long offset) { return new CastedAsUBytePointer(pointer, this.offset + offset); }

    @Override public byte getRawByte(long index) { return pointer.getRawByte(offset + index); }
    @Override public void setRawByte(long index, byte value) { pointer.setRawByte(offset + index, value); }
    @Override public Pointer<Boolean> toBool() { return new CastedAsBooleanPointer(pointer, offset); }
    @Override public Pointer<Byte> toByte() { return new CastedAsBytePointer(pointer, offset); }
    @Override public Pointer<UByte> toUByte() { return this; }
}
