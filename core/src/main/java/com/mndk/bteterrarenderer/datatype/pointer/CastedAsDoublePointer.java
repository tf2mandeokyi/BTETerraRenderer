package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.ULong;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

class CastedAsDoublePointer extends CastedPointer<Double> implements RawLongPointer {
    CastedAsDoublePointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<Double> getType() { return DataType.float64(); }
    @Override public Double get() { return fromRaw(pointer.getRawLong(offset)); }
    @Override public Double get(long index) { return fromRaw(pointer.getRawLong(offset + index)); }
    @Override public void set(Double value) { pointer.setRawLong(offset, toRaw(value)); }
    @Override public void set(long index, Double value) { pointer.setRawLong(offset + index, toRaw(value)); }
    @Override public Pointer<Double> add(long offset) { return new CastedAsDoublePointer(pointer, this.offset + offset); }

    @Override public long getRawLong(long index) { return pointer.getRawLong(offset + index); }
    @Override public void setRawLong(long index, long value) { pointer.setRawLong(offset + index, value); }
    @Override public Pointer<Long> toLong() { return new CastedAsLongPointer(pointer, offset); }
    @Override public Pointer<ULong> toULong() { return new CastedAsULongPointer(pointer, offset); }
    @Override public Pointer<Double> toDouble() { return this; }
}
