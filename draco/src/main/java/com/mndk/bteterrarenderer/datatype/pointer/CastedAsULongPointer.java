package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.ULong;

class CastedAsULongPointer extends CastedPointer<ULong> implements RawLongPointer {
    CastedAsULongPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<ULong> getType() { return DataType.uint64(); }
    @Override public ULong get() { return ULong.of(pointer.getRawLong(offset)); }
    @Override public ULong get(long index) { return ULong.of(pointer.getRawLong(offset + index)); }
    @Override public void set(ULong value) { pointer.setRawLong(offset, value); }
    @Override public void set(long index, ULong value) { pointer.setRawLong(offset + index, value); }
    @Override public Pointer<ULong> add(long offset) { return new CastedAsULongPointer(pointer, this.offset + offset); }

    @Override public long getRawLong(long index) { return pointer.getRawLong(offset + index); }
    @Override public void setRawLong(long index, long value) { pointer.setRawLong(offset + index, value); }
    @Override public Pointer<Long> toLong() { return new CastedAsLongPointer(pointer, offset); }
    @Override public Pointer<ULong> toULong() { return this; }
    @Override public Pointer<Double> toDouble() { return new CastedAsDoublePointer(pointer, offset); }
}
