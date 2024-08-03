package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.FloatComparator;

import javax.annotation.Nullable;
import java.util.Comparator;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

@Getter
@RequiredArgsConstructor
class BorrowedFloatArray extends BorrowedArray<Float> implements RawIntPointer {
    private final float[] array;
    private final int offset;

    @Override public DataType<Float> getType() { return DataType.float32(); }
    @Override public Float get() { return array[offset]; }
    @Override public Float get(int index) { return array[this.offset + index]; }
    @Override public void set(Float value) { array[offset] = value; }
    @Override public void set(int index, Float value) { array[this.offset + index] = value; }
    @Override public Pointer<Float> add(int offset) { return new BorrowedFloatArray(array, this.offset + offset); }
    @Override public RawPointer asRaw() { return this; }
    @Override public void sort(int length, @Nullable Comparator<Float> comparator) {
        FloatComparator objectComparator = comparator == null ? Float::compare : comparator::compare;
        Primitive.sort(array, this.offset, this.offset + length, objectComparator);
    }
    @Override public void swap(int a, int b) {
        float temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    @Override public int getRawInt(long index) { return toRaw(array[checkIndex(offset + index)]); }
    @Override public void setRawInt(long index, int value) { array[checkIndex(offset + index)] = fromRaw(value); }
    @Override public Pointer<Float> toFloat() { return this; }
}
