package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractBorrowedRawShortArray<E> extends BorrowedArray<E> implements RawShortPointer {
    protected final short[] array;
    protected final int offset;

    @Override public final E get() { return fromRaw(array[offset]); }
    @Override public final void set(E value) { array[offset] = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }
    @Override protected final E get(int index) { return fromRaw(array[this.offset + index]); }
    @Override protected final void set(int index, E value) { array[this.offset + index] = toRaw(value); }
    @Override public final void swap(int a, int b) {
        short temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    protected abstract short toRaw(E value);
    protected abstract E fromRaw(short raw);
    @Override public final short getRawShort(long index) { return array[DataType.intLimit(offset + index)]; }
    @Override public final void setRawShort(long index, short value) { array[DataType.intLimit(offset + index)] = value; }
}
