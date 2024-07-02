package com.mndk.bteterrarenderer.draco.core.vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

class CppVectorOffsetView<E> implements CppVector<E> {

    private final CppVector<E> delegate;
    private final int offset;

    CppVectorOffsetView(CppVector<E> delegate, int offset) {
        if(offset < 0) throw new IllegalArgumentException("Offset must be non-negative");
        this.delegate = delegate;
        this.offset = offset;
    }

    @Override public void assign(int count, E value) {
        throw new UnsupportedOperationException("Not implemented");
    }
    @Override public void assign(Function<Integer, E> values, Integer start, Integer end) {
        throw new UnsupportedOperationException("Not implemented");
    }
    @Override public int size() { return delegate.size() - offset; }
    @Override public E front() { return delegate.get(offset); }
    @Override public E back() { return delegate.back(); }
    @Override public E get(Integer index) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("index = " + index + ", size = " + size());
        return delegate.get(index + offset);
    }
    @Override public boolean contains(E value) {
        for(int i = 0; i < size(); i++) {
            if(get(i).equals(value)) return true;
        }
        return false;
    }
    @Override public E set(Integer index, E value) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("index = " + index + ", size = " + size());
        return delegate.set(index + offset, value);
    }
    @Override public void insert(Integer index, E value) {
        if(index < 0 || index > size()) throw new IndexOutOfBoundsException("index = " + index + ", size = " + size());
        delegate.insert(index + offset, value);
    }
    @Override public void pushBack(E value) { delegate.pushBack(value); }
    @Override public void clear() { delegate.clear(); }
    @Override public void erase(Integer index) { delegate.erase(index + offset); }
    @Override public E popBack() { return delegate.popBack(); }
    @Override public void swap(CppVector<E> other) {
        throw new UnsupportedOperationException("Not implemented");
    }
    @Override public void sort(@Nullable Comparator<E> comparator) {
        throw new UnsupportedOperationException("Not implemented");
    }
    @Override public void reserve(int minCapacity) {
        delegate.reserve(minCapacity + offset);
    }
    @Override public void resize(int size) {
        if(size < 0) throw new IllegalArgumentException("Size must be non-negative: given = " + size);
        delegate.resize(size + offset);
    }
    @Override public void resize(int size, E value) {
        if(size < 0) throw new IllegalArgumentException("Size must be non-negative: given = " + size);
        delegate.resize(size + offset, value);
    }
    @Override public void resize(int size, Supplier<E> value) {
        if(size < 0) throw new IllegalArgumentException("Size must be non-negative: given = " + size);
        delegate.resize(size + offset, value);
    }
    @Override public void resize(int size, Function<Integer, E> values) {
        if(size < 0) throw new IllegalArgumentException("Size must be non-negative: given = " + size);
        delegate.resize(size + offset, i -> values.apply(i - offset));
    }
    @Nonnull @Override public Iterator<E> iterator() {
        return new ListIterator();
    }
    private class ListIterator implements Iterator<E> {
        private int index = 0;
        @Override public boolean hasNext() { return index < size(); }
        @Override public E next() { return get(index++); }
    }
}
