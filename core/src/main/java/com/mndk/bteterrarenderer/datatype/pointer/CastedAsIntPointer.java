package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;

class CastedAsIntPointer extends CastedPointer<Integer> implements RawIntPointer {
    CastedAsIntPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<Integer> getType() { return DataType.int32(); }
    @Override public Integer get() { return pointer.getRawInt(offset); }
    @Override public Integer get(long index) { return pointer.getRawInt(offset + index); }
    @Override public void set(Integer value) { pointer.setRawInt(offset, value); }
    @Override public void set(long index, Integer value) { pointer.setRawInt(offset + index, value); }
    @Override public Pointer<Integer> add(long offset) { return new CastedAsIntPointer(pointer, this.offset + offset); }

    @Override public int getRawInt(long index) { return pointer.getRawInt(offset + index); }
    @Override public void setRawInt(long index, int value) { pointer.setRawInt(offset + index, value); }
    @Override public Pointer<Integer> asRawToInt() { return this; }
    @Override public Pointer<UInt> asRawToUInt() { return new CastedAsUIntPointer(pointer, offset); }
    @Override public Pointer<Float> toFloat() { return new CastedAsFloatPointer(pointer, offset); }
}
