package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

class CastedAsBooleanPointer extends CastedPointer<Boolean> implements RawBytePointer {
    CastedAsBooleanPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<Boolean> getType() { return DataType.bool(); }
    @Override public Boolean get() { return fromRaw(pointer.getRawByte(offset)); }
    @Override public Boolean get(long index) { return fromRaw(pointer.getRawByte(offset + index)); }
    @Override public void set(Boolean value) { pointer.setRawByte(offset, toRaw(value)); }
    @Override public void set(long index, Boolean value) { pointer.setRawByte(offset + index, toRaw(value)); }
    @Override public Pointer<Boolean> add(long offset) { return new CastedAsBooleanPointer(pointer, this.offset + offset); }

    @Override public byte getRawByte(long index) { return pointer.getRawByte(offset + index); }
    @Override public void setRawByte(long index, byte value) { pointer.setRawByte(offset + index, value); }
    @Override public Pointer<Boolean> toBool() { return this; }
    @Override public Pointer<Byte> toByte() { return new CastedAsBytePointer(pointer, offset); }
    @Override public Pointer<UByte> toUByte() { return new CastedAsUBytePointer(pointer, offset); }
}
