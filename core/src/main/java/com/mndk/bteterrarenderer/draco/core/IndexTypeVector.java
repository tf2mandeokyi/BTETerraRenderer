package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class IndexTypeVector<I extends IndexType<I>, E> implements Iterable<E> {

    private final CppVector<E> vector;

    public IndexTypeVector(DataType<E> type) {
        this.vector = new CppVector<>(type);
    }
    public IndexTypeVector(DataType<E> type, int size) {
        this.vector = new CppVector<>(type, size);
    }
    public IndexTypeVector(DataType<E> type, int size, E value) {
        this.vector = new CppVector<>(type, size, value);
    }

    public IndexTypeVector(Supplier<E> defaultValueMaker) {
        this.vector = new CppVector<>(DataType.object(defaultValueMaker));
    }
    public IndexTypeVector(Supplier<E> defaultValueMaker, int size) {
        this.vector = new CppVector<>(DataType.object(defaultValueMaker), size);
    }

    public void clear() { vector.clear(); }
    public void reserve(int size) { vector.reserve(size); }
    public void resize(int size) { vector.resize(size); }
    public void resize(int size, E value) { vector.resize(size, value); }
    public void assign(int size, E value) { vector.assign(size, value); }
    public void erase(I index) { vector.erase(index.getValue()); }

    public void swap(IndexTypeVector<I, E> other) { vector.swap(other.vector); }

    public int size() { return vector.size(); }
    public boolean isEmpty() { return vector.isEmpty(); }

    public void pushBack(E value) { vector.pushBack(value); }

    public E get(I index) { return vector.get(index.getValue()); }
    public void set(I index, E value) { vector.set(index.getValue(), value); }
    public void set(I index, Function<E, E> setter) { vector.set(index.getValue(), setter); }

    @Override
    @Nonnull
    public Iterator<E> iterator() { return vector.iterator(); }
}
