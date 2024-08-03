package com.mndk.bteterrarenderer.datatype.pointer;

import javax.annotation.Nullable;
import java.util.Comparator;

public abstract class SingleVariablePointer<T> implements Pointer<T> {

    protected final void checkIndex(long index) {
        if(index == 0) return;
        throw new IndexOutOfBoundsException("index = " + index);
    }

    @Override public final Pointer<T> add(long offset) { checkIndex(offset); return this; }

    @Override public final String toString() {
        int origin = System.identityHashCode(this);
        return "Pointer<" + getType() + ">[" + String.format("%08x", origin) + ']';
    }
    @Override public final int hashCode() { return System.identityHashCode(this); }
    @Override public final boolean equals(Object obj) { return this == obj; }

    @Override public final boolean nextPermutation(long length, @Nullable Comparator<T> comparator) {
        if(length <= 1) return false;
        throw new UnsupportedOperationException("Permutation is not supported for single variable pointers");
    }
    @Override public final void swap(long a, long b) {
        if(a == 0 && b == 0) return;
        throw new UnsupportedOperationException("Cannot swap a single variable");
    }
    @Override public final void sort(int length, @Nullable Comparator<T> comparator) {
        if(length <= 1) return;
        throw new UnsupportedOperationException("Sorting is not supported for single variable pointers");
    }
    @Override public final int contentHashCode(long length) {
        if(length == 0) return 0;
        if(length > 1) throw new UnsupportedOperationException("Cannot hash multiple elements");
        return get().hashCode();
    }
    @Override public final boolean contentEquals(Pointer<T> other, long length) {
        if(length == 0) return true;
        if(length > 1) throw new UnsupportedOperationException("Cannot compare multiple elements");
        return get().equals(other.get());
    }
}
