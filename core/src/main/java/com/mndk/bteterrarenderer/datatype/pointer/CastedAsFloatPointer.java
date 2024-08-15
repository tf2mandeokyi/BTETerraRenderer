package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

class CastedAsFloatPointer extends CastedPointer<Float> implements RawIntPointer {
    CastedAsFloatPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<Float> getType() { return DataType.float32(); }
    @Override public Float get() { return fromRaw(pointer.getRawInt(offset)); }
    @Override public Float get(long index) { return fromRaw(pointer.getRawInt(offset + index)); }
    @Override public void set(Float value) { pointer.setRawInt(offset, toRaw(value)); }
    @Override public void set(long index, Float value) { pointer.setRawInt(offset + index, toRaw(value)); }
    @Override public Pointer<Float> add(long offset) { return new CastedAsFloatPointer(pointer, this.offset + offset); }

    @Override public int getRawInt(long index) { return pointer.getRawInt(offset + index); }
    @Override public void setRawInt(long index, int value) { pointer.setRawInt(offset + index, value); }
    @Override public Pointer<Integer> toInt() { return new CastedAsIntPointer(pointer, offset); }
    @Override public Pointer<UInt> toUInt() { return new CastedAsUIntPointer(pointer, offset); }
    @Override public Pointer<Float> toFloat() { return this; }
}
