package com.mndk.bteterrarenderer.draco.core.vector;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
abstract class CppVectorCastView<InE, OutE> implements CppVector<OutE> {

    private final CppVector<InE> delegate;

    protected abstract InE inBound(OutE value);
    protected abstract OutE outBound(InE value);

    @Override public void assign(int count, OutE value) {
        delegate.assign(count, this.inBound(value));
    }
    @Override public void assign(Function<Integer, OutE> values, Integer start, Integer end) {
        delegate.assign(index -> this.inBound(values.apply(index)), start, end);
    }
    @Override public int size() { return delegate.size(); }
    @Override public OutE front() { return this.outBound(delegate.front()); }
    @Override public OutE back() { return this.outBound(delegate.back()); }
    @Override public OutE get(Integer index) { return this.outBound(delegate.get(index)); }
    @Override public boolean contains(OutE value) { return delegate.contains(this.inBound(value)); }
    @Override public OutE set(Integer index, OutE value) { return this.outBound(delegate.set(index, this.inBound(value))); }
    @Override public void insert(Integer index, OutE value) { delegate.insert(index, this.inBound(value)); }
    @Override public void pushBack(OutE value) { delegate.pushBack(this.inBound(value)); }
    @Override public void clear() { delegate.clear(); }
    @Override public void erase(Integer index) { delegate.erase(index); }
    @Override public OutE popBack() { return this.outBound(delegate.popBack()); }
    @Override public void swap(CppVector<OutE> other) {
        CppVector<InE> temp = CppVector.create(other.size());
        for(int i = 0; i < other.size(); i++) {
            temp.set(i, this.inBound(other.get(i)));
        }
        delegate.swap(temp);
        for(int i = 0; i < other.size(); i++) {
            other.set(i, this.outBound(temp.get(i)));
        }
    }
    @Override public void sort(@Nullable Comparator<OutE> comparator) {
        Comparator<InE> inComparator = comparator == null ?
                null :
                (o1, o2) -> comparator.compare(this.outBound(o1), this.outBound(o2));
        delegate.sort(inComparator);
    }
    @Override public void reserve(int minCapacity) { delegate.reserve(minCapacity); }
    @Override public void resize(int size) { delegate.resize(size); }
    @Override public void resize(int size, OutE value) { delegate.resize(size, this.inBound(value)); }
    @Override public void resize(int size, Supplier<OutE> value) {
        delegate.resize(size, () -> this.inBound(value.get()));
    }
    @Override public void resize(int size, Function<Integer, OutE> values) {
        delegate.resize(size, index -> this.inBound(values.apply(index)));
    }
    @Nonnull
    @Override public Iterator<OutE> iterator() {
        return new ListIterator();
    }

    private class ListIterator implements Iterator<OutE> {
        private int index = 0;
        @Override public boolean hasNext() { return index < size(); }
        @Override public OutE next() { return get(index++); }
    }
}
