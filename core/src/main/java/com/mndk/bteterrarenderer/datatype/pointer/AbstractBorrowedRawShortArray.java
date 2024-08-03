package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.ShortComparator;

import javax.annotation.Nullable;
import java.util.Comparator;

@Getter
@RequiredArgsConstructor
public abstract class AbstractBorrowedRawShortArray<E> extends BorrowedArray<E> implements RawShortPointer {
    protected final short[] array;
    protected final int offset;

    @Override public final E get() { return fromRaw(array[offset]); }
    @Override public final E get(int index) { return fromRaw(array[this.offset + index]); }
    @Override public final void set(E value) { array[offset] = toRaw(value); }
    @Override public final void set(int index, E value) { array[this.offset + index] = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }
    @Override public final void sort(int length, @Nullable Comparator<E> comparator) {
        ShortComparator objectComparator = comparator == null
                ? Short::compare : (a, b) -> comparator.compare(fromRaw(a), fromRaw(b));
        Primitive.sort(array, this.offset, this.offset + length, objectComparator);
    }
    @Override public final void swap(int a, int b) {
        short temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    protected abstract short toRaw(E value);
    protected abstract E fromRaw(short raw);
    @Override public final short getRawShort(long index) { return array[checkIndex(offset + index)]; }
    @Override public final void setRawShort(long index, short value) { array[checkIndex(offset + index)] = value; }
}
