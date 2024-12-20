package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractBorrowedRawIntArray<E> extends BorrowedArray<E> implements RawIntPointer {
    protected final int[] array;
    protected final int offset;

    @Override public final E get() { return fromRaw(array[offset]); }
    @Override public final void set(E value) { array[offset] = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }
    @Override protected final E get(int index) { return fromRaw(array[this.offset + index]); }
    @Override protected final void set(int index, E value) { array[this.offset + index] = toRaw(value); }
    @Override public final void swap(int a, int b) {
        int temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    protected abstract int toRaw(E value);
    protected abstract E fromRaw(int raw);
    @Override public final int getRawInt(long index) { return array[DataType.intLimit(offset + index)]; }
    @Override public final void setRawInt(long index, int value) { array[DataType.intLimit(offset + index)] = value; }

    @Override public Pointer<Integer> toInt() { return new BorrowedIntArray(array, offset); }
    @Override public Pointer<UInt> toUInt() { return new BorrowedUIntArray(array, offset); }
}
