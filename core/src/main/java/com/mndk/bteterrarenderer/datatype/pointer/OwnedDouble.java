package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AllArgsConstructor;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

@AllArgsConstructor
class OwnedDouble extends SingleVariablePointer<Double> implements RawLongPointer {
    private double value;

    @Override public DataType<Double> getType() { return DataType.float64(); }
    @Override public Double get() { return value; }
    @Override public void set(Double value) { this.value = value; }
    @Override public RawPointer asRaw() { return this; }

    @Override public long getRawLong(long index) { checkIndex(index); return toRaw(value); }
    @Override public void setRawLong(long index, long value) { checkIndex(index); this.value = fromRaw(value); }
    @Override public Pointer<Double> toDouble() { return this; }
}
