package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AllArgsConstructor;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

@AllArgsConstructor
class OwnedBoolean extends SingleVariablePointer<Boolean> implements RawBytePointer {
    private boolean value;

    @Override public DataType<Boolean> getType() { return DataType.bool(); }
    @Override public Boolean get() { return value; }
    @Override public Boolean get(long index) { checkIndex(index); return value; }
    @Override public void set(Boolean value) { this.value = value; }
    @Override public void set(long index, Boolean value) { checkIndex(index); this.value = value; }
    @Override public RawPointer asRaw() { return this; }

    @Override public byte getRawByte(long index) { checkIndex(index); return toRaw(value); }
    @Override public void setRawByte(long index, byte value) { checkIndex(index); this.value = fromRaw(value); }
    @Override public Pointer<Boolean> toBool() { return this; }
}
