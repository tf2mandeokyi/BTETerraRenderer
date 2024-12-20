package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractBorrowedRawLongArray<E> extends BorrowedArray<E> implements RawLongPointer {
    protected final long[] array;
    protected final int offset;

    @Override public final E get() { return fromRaw(array[offset]); }
    @Override public final void set(E value) { array[offset] = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }
    @Override protected final E get(int index) { return fromRaw(array[this.offset + index]); }
    @Override protected final void set(int index, E value) { array[this.offset + index] = toRaw(value); }
    @Override protected final void swap(int a, int b) {
        long temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    protected abstract long toRaw(E value);
    protected abstract E fromRaw(long raw);
    @Override public final long getRawLong(long index) { return array[DataType.intLimit(offset + index)]; }
    @Override public final void setRawLong(long index, long value) { array[DataType.intLimit(offset + index)] = value; }

    @Override public Pointer<Long> toLong() { return new BorrowedLongArray(array, offset); }
    @Override public Pointer<ULong> toULong() { return new BorrowedULongArray(array, offset); }
}
