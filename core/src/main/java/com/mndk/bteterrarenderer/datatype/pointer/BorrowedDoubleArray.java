package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.DoubleComparator;

import javax.annotation.Nullable;
import java.util.Comparator;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

@Getter
@RequiredArgsConstructor
class BorrowedDoubleArray extends BorrowedArray<Double> implements RawLongPointer {
    private final double[] array;
    private final int offset;

    @Override public DataType<Double> getType() { return DataType.float64(); }
    @Override public Double get() { return array[offset]; }
    @Override public Double get(int index) { return array[this.offset + index]; }
    @Override public void set(Double value) { array[offset] = value; }
    @Override public void set(int index, Double value) { array[this.offset + index] = value; }
    @Override public Pointer<Double> add(int offset) { return new BorrowedDoubleArray(array, this.offset + offset); }
    @Override public RawPointer asRaw() { return this; }
    @Override public void sort(int length, @Nullable Comparator<Double> comparator) {
        DoubleComparator objectComparator = comparator == null ? Double::compare : comparator::compare;
        Primitive.sort(array, offset, offset + length, objectComparator);
    }
    @Override public void swap(int a, int b) {
        double temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    @Override public long getRawLong(long index) { return toRaw(array[checkIndex(offset + index)]); }
    @Override public void setRawLong(long index, long value) { array[checkIndex(offset + index)] = fromRaw(value); }
    @Override public Pointer<Double> toDouble() { return this; }
}
