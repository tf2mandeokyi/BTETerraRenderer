package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

public interface BigArray<E> {

    int MAX_INNER_SIZE = Integer.MAX_VALUE - 8;

    E get(long index);
    void set(long index, E value);
    long size();
    void copyTo(long srcIndex, BigArray<E> dest, long dstIndex, long size);
    boolean equals(Object obj);
    boolean equals(BigArray<E> other);
    int hashCode();
    int hashCode(long offset, long length);
    String toString();

    default RawPointer getRawPointer() { return this.getRawPointer(0); }
    RawPointer getRawPointer(long byteOffset);
}
