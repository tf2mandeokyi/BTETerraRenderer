package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.ULong;

class CastedAsLongPointer extends CastedPointer<Long> implements RawLongPointer {
    CastedAsLongPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<Long> getType() { return DataType.int64(); }
    @Override public Long get() { return pointer.getRawLong(offset); }
    @Override public Long get(long index) { return pointer.getRawLong(offset + index); }
    @Override public void set(Long value) { pointer.setRawLong(offset, value); }
    @Override public void set(long index, Long value) { pointer.setRawLong(offset + index, value); }
    @Override public Pointer<Long> add(long offset) { return new CastedAsLongPointer(pointer, this.offset + offset); }

    @Override public long getRawLong(long index) { return pointer.getRawLong(offset + index); }
    @Override public void setRawLong(long index, long value) { pointer.setRawLong(offset + index, value); }
    @Override public Pointer<Long> toLong() { return this; }
    @Override public Pointer<ULong> toULong() { return new CastedAsULongPointer(pointer, offset); }
    @Override public Pointer<Double> toDouble() { return new CastedAsDoublePointer(pointer, offset); }
}
