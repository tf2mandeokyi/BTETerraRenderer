package com.mndk.bteterrarenderer.datatype.pointer;

import java.util.Objects;

public abstract class BorrowedArray<T> implements Pointer<T> {

    protected abstract Object getArray();
    protected abstract int getOffset();

    protected abstract T get(int index);
    protected abstract void set(int index, T value);
    protected abstract Pointer<T> add(int offset);
    protected abstract void swap(int a, int b);

    protected final int checkIndex(long index) {
        if(index >> 32 == 0) return (int) index;
        throw new IndexOutOfBoundsException("Index out of bounds: " + index);
    }
    @Override public final T get(long index) { return get(checkIndex(index)); }
    @Override public final void set(long index, T value) { set(checkIndex(index), value); }
    @Override public final Pointer<T> add(long offset) { return add(checkIndex(offset)); }
    @Override public final Object getOrigin() { return getArray(); }
    @Override public final void swap(long a, long b) { swap(checkIndex(a), checkIndex(b)); }

    @Override public final String toString() {
        // We only print the array address and offset.
        // Since there is no "array address" in java, we print the array's hash code instead.
        int origin = System.identityHashCode(getArray());
        return "Pointer<" + getType() + ">[" + String.format("%08x", origin) + "+" + getOffset() + ']';
    }
    @Override public final int hashCode() {
        // We only hash the array address and offset.
        return Objects.hash(System.identityHashCode(getArray()), getOffset());
    }
    @Override public final boolean equals(Object obj) {
        // We only compare the array address and offset, just like how C++ compares pointers
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BorrowedArray<?> that = (BorrowedArray<?>) obj;
        return getArray() == that.getArray() && getOffset() == that.getOffset();
    }
}
