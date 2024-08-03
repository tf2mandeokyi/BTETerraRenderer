package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;

class CastedAsUIntPointer extends CastedPointer<UInt> implements RawIntPointer {
    CastedAsUIntPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<UInt> getType() { return DataType.uint32(); }
    @Override public UInt get() { return UInt.of(pointer.getRawInt(offset)); }
    @Override public UInt get(long index) { return UInt.of(pointer.getRawInt(offset + index)); }
    @Override public void set(UInt value) { pointer.setRawInt(offset, value); }
    @Override public void set(long index, UInt value) { pointer.setRawInt(offset + index, value); }
    @Override public Pointer<UInt> add(long offset) { return new CastedAsUIntPointer(pointer, this.offset + offset); }

    @Override public int getRawInt(long index) { return pointer.getRawInt(offset + index); }
    @Override public void setRawInt(long index, int value) { pointer.setRawInt(offset + index, value); }
    @Override public Pointer<Integer> asRawToInt() { return new CastedAsIntPointer(pointer, offset); }
    @Override public Pointer<UInt> asRawToUInt() { return this; }
    @Override public Pointer<Float> toFloat() { return new CastedAsFloatPointer(pointer, offset); }
}
