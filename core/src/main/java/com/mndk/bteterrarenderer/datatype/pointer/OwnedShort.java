package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class OwnedShort extends SingleVariablePointer<Short> implements RawShortPointer {
    private short value;

    @Override public DataType<Short> getType() { return DataType.int16(); }
    @Override public Short get() { return value; }
    @Override public Short get(long index) { checkIndex(index); return value; }
    @Override public void set(Short value) { this.value = value; }
    @Override public void set(long index, Short value) { checkIndex(index); this.value = value; }
    @Override public RawPointer asRaw() { return this; }

    @Override public short getRawShort(long index) { checkIndex(index); return value; }
    @Override public void setRawShort(long index, short value) { checkIndex(index); this.value = value; }
    @Override public Pointer<Short> toShort() { return this; }
}
