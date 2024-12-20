package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UShort;

class CastedAsShortPointer extends CastedPointer<Short> implements RawShortPointer {
    CastedAsShortPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<Short> getType() { return DataType.int16(); }
    @Override public Short get() { return pointer.getRawShort(offset); }
    @Override public Short get(long index) { return pointer.getRawShort(offset + index); }
    @Override public void set(Short value) { pointer.setRawShort(offset, value); }
    @Override public void set(long index, Short value) { pointer.setRawShort(offset + index, value); }
    @Override public Pointer<Short> add(long offset) { return new CastedAsShortPointer(pointer, this.offset + offset); }

    @Override public short getRawShort(long index) { return pointer.getRawShort(offset + index); }
    @Override public void setRawShort(long index, short value) { pointer.setRawShort(offset + index, value); }
    @Override public Pointer<Short> toShort() { return this; }
    @Override public Pointer<UShort> toUShort() { return new CastedAsUShortPointer(pointer, offset); }
}
