package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@RequiredArgsConstructor
class PointerIterator<T> implements Iterator<T> {
    private final Pointer<T> pointer;
    private final long size;
    private long index = 0;
    @Override public boolean hasNext() { return index < size; }
    @Override public T next() { return pointer.get(index++); }
}