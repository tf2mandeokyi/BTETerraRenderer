package com.mndk.bteterrarenderer.draco.core;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("SuspiciousMethodCalls")
abstract class CppVectorInternal<I, E> implements Iterable<E> {

    private final List<E> list = new ArrayList<>();

    public CppVectorInternal() {}
    public CppVectorInternal(int size, E value) {
        this.resize(size, value);
    }
    public CppVectorInternal(CppVectorInternal<I, E> other, I start, I end) {
        this.assign(other::get, start, end);
    }

    @Override
    @Nonnull
    public java.util.Iterator<E> iterator() {
        return this.list.iterator();
    }

    public void clear() {
        list.clear();
    }

    public void resize(int size) {
        if     (size > this.size()) for (int i = this.size(); i < size; i++) this.pushBack(null);
        else if(size < this.size()) for (int i = this.size(); i > size; i--) this.popBack();
    }
    public void resize(int size, E value) {
        if     (size > this.size()) for (int i = this.size(); i < size; i++) this.pushBack(value);
        else if(size < this.size()) for (int i = this.size(); i > size; i--) this.popBack();
    }

    public void assign(int count, E value) {
        this.clear();
        for(int i = 0; i < count; i++) this.pushBack(value);
    }

    public void assign(Function<I, E> values, I start, I end) {
        this.clear();
        Iterator<I> iterator = this.newIterator(start, end);
        while(iterator.hasNext()) this.pushBack(values.apply(iterator.next()));
    }

    public void insert(I index, E value) {
        list.add(getIndexValue(index), value);
    }

    public E erase(I index) {
        return list.remove(getIndexValue(index));
    }

    public int size() { return list.size(); }
    public boolean isEmpty() { return list.isEmpty(); }

    public boolean pushBack(E value) { return list.add(value); }
    @SuppressWarnings("UnusedReturnValue")
    public E popBack() { return list.remove(this.size() - 1); }

    @SafeVarargs
    public final void emplaceBack(E... values) { list.addAll(java.util.Arrays.asList(values)); }

    public E get(I index) {
        return list.get(getIndexValue(index));
    }
    public E set(I index, E value) {
        return list.set(getIndexValue(index), value);
    }
    public I find(Object o) {
        return integerToIndex(list.indexOf(o));
    }
    public I findBackwards(Object o) {
        return integerToIndex(list.lastIndexOf(o));
    }

    protected abstract int getIndexValue(I index);
    protected abstract I integerToIndex(int index);
    protected abstract Iterator<I> newIterator(I start, I end);

    public boolean contains(Object element) { return list.contains(element); }
    @Nonnull
    public Object[] toArray() { return list.toArray(); }
    @Nonnull
    public <T> T[] toArray(@Nonnull T[] a) { return list.toArray(a); }
    public boolean remove(Object o) { return list.remove(o); }
    @SuppressWarnings("SlowListContainsAll")
    public boolean containsAll(@Nonnull Collection<?> c) { return list.containsAll(c); }
    public boolean addAll(@Nonnull Collection<? extends E> c) { return list.addAll(c); }
    public boolean addAll(int index, @Nonnull Collection<? extends E> c) { return list.addAll(index, c); }
    public boolean removeAll(@Nonnull Collection<?> c) { return list.removeAll(c); }
    public boolean retainAll(@Nonnull Collection<?> c) { return list.retainAll(c); }
    @Nonnull
    public ListIterator<E> listIterator() { return list.listIterator(); }
    @Nonnull
    public ListIterator<E> listIterator(I index) { return list.listIterator(this.getIndexValue(index)); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CppVectorInternal<?, ?> that = (CppVectorInternal<?, ?>) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }

    @Override
    public String toString() {
        return this.getClass() + list.toString();
    }
}
