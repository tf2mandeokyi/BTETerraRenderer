package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AllArgsConstructor;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

@AllArgsConstructor
class OwnedFloat extends SingleVariablePointer<Float> implements RawIntPointer {
    private float value;

    @Override public DataType<Float> getType() { return DataType.float32(); }
    @Override public Float get() { return value; }
    @Override public Float get(long index) { checkIndex(index); return value; }
    @Override public void set(Float value) { this.value = value; }
    @Override public void set(long index, Float value) { checkIndex(index); this.value = value; }
    @Override public RawPointer asRaw() { return this; }

    @Override public int getRawInt(long index) { checkIndex(index); return toRaw(value); }
    @Override public void setRawInt(long index, int value) { checkIndex(index); this.value = fromRaw(value); }
    @Override public Pointer<Float> toFloat() { return this; }
}
