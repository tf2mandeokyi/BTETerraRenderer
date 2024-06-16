package com.mndk.bteterrarenderer.draco.core;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.IntStream;

public class CppVector<E> extends CppVectorInternal<Integer, E> implements List<E> {

    public CppVector() {
        super();
    }

    public CppVector(CppVector<E> other, int start, int end) {
        super(other, start, end);
    }

    @Override
    protected int getIndexValue(Integer index) {
        return index;
    }

    @Override
    protected Integer integerToIndex(int index) {
        return index;
    }

    @Override
    protected Iterator<Integer> newIterator(Integer start, Integer end) {
        return IntStream.range(start, end).iterator();
    }

    @Override
    public boolean add(E e) {
        return this.pushBack(e);
    }

    @Override
    public E get(int index) {
        return super.get(index);
    }

    @Override
    public E set(int index, E element) {
        return super.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        super.insert(index, element);
    }

    @Override
    public E remove(int index) {
        return super.erase(index);
    }

    @Override
    public int indexOf(Object o) {
        return super.find(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return super.findBackwards(o);
    }

    @Nonnull
    @Override
    public ListIterator<E> listIterator(int index) {
        return super.listIterator(index);
    }

    @Nonnull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new CppVector<>(this, fromIndex, toIndex);
    }
}
