package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UShort;

class CastedAsUShortPointer extends CastedPointer<UShort> implements RawShortPointer {
    CastedAsUShortPointer(RawPointer pointer, long offset) { super(pointer, offset); }

    @Override public DataType<UShort> getType() { return DataType.uint16(); }
    @Override public UShort get() { return UShort.of(pointer.getRawShort(offset)); }
    @Override public UShort get(long index) { return UShort.of(pointer.getRawShort(offset + index)); }
    @Override public void set(UShort value) { pointer.setRawShort(offset, value); }
    @Override public void set(long index, UShort value) { pointer.setRawShort(offset + index, value); }
    @Override public Pointer<UShort> add(long offset) { return new CastedAsUShortPointer(pointer, this.offset + offset); }

    @Override public short getRawShort(long index) { return pointer.getRawShort(offset + index); }
    @Override public void setRawShort(long index, short value) { pointer.setRawShort(offset + index, value); }
    @Override public Pointer<Short> toShort() { return new CastedAsShortPointer(pointer, offset); }
    @Override public Pointer<UShort> toUShort() { return this; }
}
