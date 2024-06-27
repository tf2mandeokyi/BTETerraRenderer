package com.mndk.bteterrarenderer.datatype.array;

public interface BigArray<E> {

    E get(long index);
    void set(long index, E value);
    long size();
    void copyTo(long srcIndex, BigArray<E> dest, long dstIndex, long size);
    boolean equals(Object obj);
    boolean equals(BigArray<E> other);
    int hashCode();
    String toString();

    default BigArray<E> withOffset(long offset) {
        return new BigArrayView<>(this, offset);
    }
}
