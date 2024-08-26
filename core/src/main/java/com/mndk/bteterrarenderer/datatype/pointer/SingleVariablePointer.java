package com.mndk.bteterrarenderer.datatype.pointer;

public abstract class SingleVariablePointer<T> implements Pointer<T> {

    protected final void checkIndex(long index) {
        if(index == 0) return;
        throw new IndexOutOfBoundsException("index = " + index);
    }

    @Override public final T get(long index) { checkIndex(index); return get(); }
    @Override public final void set(long index, T value) { checkIndex(index); set(value); }
    @Override public final Pointer<T> add(long offset) { checkIndex(offset); return this; }
    @Override public final Object getOrigin() { return this; }

    @Override public final String toString() {
        int origin = System.identityHashCode(this);
        return "Pointer<" + getType() + ">{hash=" + String.format("%08x", origin) + ",value=" + get() + "}";
    }
    @Override public final int hashCode() { return System.identityHashCode(this); }
    @Override public final boolean equals(Object obj) { return this == obj; }

    @Override public final void swap(long a, long b) {
        if(a == 0 && b == 0) return;
        throw new UnsupportedOperationException("Cannot swap a single variable");
    }
}
