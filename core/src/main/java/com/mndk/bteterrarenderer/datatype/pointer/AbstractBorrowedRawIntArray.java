package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.IntComparator;

import javax.annotation.Nullable;
import java.util.Comparator;

@Getter
@RequiredArgsConstructor
public abstract class AbstractBorrowedRawIntArray<E> extends BorrowedArray<E> implements RawIntPointer {
    protected final int[] array;
    protected final int offset;

    @Override public final E get() { return fromRaw(array[offset]); }
    @Override public final E get(int index) { return fromRaw(array[this.offset + index]); }
    @Override public final void set(E value) { array[offset] = toRaw(value); }
    @Override public final void set(int index, E value) { array[this.offset + index] = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }
    @Override public final void sort(int length, @Nullable Comparator<E> comparator) {
        IntComparator objectComparator = comparator == null
                ? Integer::compare : (a, b) -> comparator.compare(fromRaw(a), fromRaw(b));
        Primitive.sort(array, this.offset, this.offset + length, objectComparator);
    }
    @Override public final void swap(int a, int b) {
        int temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    protected abstract int toRaw(E value);
    protected abstract E fromRaw(int raw);
    @Override public final int getRawInt(long index) { return array[checkIndex(offset + index)]; }
    @Override public final void setRawInt(long index, int value) { array[checkIndex(offset + index)] = value; }
}
