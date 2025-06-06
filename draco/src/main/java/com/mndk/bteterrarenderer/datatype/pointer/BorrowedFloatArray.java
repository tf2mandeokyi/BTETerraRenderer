package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

@Getter
@RequiredArgsConstructor
class BorrowedFloatArray extends BorrowedArray<Float> implements RawIntPointer {
    private final float[] array;
    private final int offset;

    @Override public DataType<Float> getType() { return DataType.float32(); }
    @Override public Float get() { return array[offset]; }
    @Override public void set(Float value) { array[offset] = value; }
    @Override public RawPointer asRaw() { return this; }
    @Override protected Float get(int index) { return array[this.offset + index]; }
    @Override protected void set(int index, Float value) { array[this.offset + index] = value; }
    @Override protected Pointer<Float> add(int offset) { return new BorrowedFloatArray(array, this.offset + offset); }
    @Override public void swap(int a, int b) {
        float temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    @Override public int getRawInt(long index) { return toRaw(array[DataType.intLimit(offset + index)]); }
    @Override public void setRawInt(long index, int value) { array[DataType.intLimit(offset + index)] = fromRaw(value); }
    @Override public Pointer<Float> toFloat() { return this; }
}
