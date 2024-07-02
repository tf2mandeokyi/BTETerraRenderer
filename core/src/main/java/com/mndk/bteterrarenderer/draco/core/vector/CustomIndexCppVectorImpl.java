package com.mndk.bteterrarenderer.draco.core.vector;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

abstract class CustomIndexCppVectorImpl<I, E, EArray, V extends CustomIndexCppVector<I, E, V>>
        implements CustomIndexCppVector<I, E, V> {

    private int size = 0;
    private EArray array; // capacity = arrayManager.length(array)
    private final DataArrayManager<E, EArray> arrayManager;

    protected CustomIndexCppVectorImpl(DataArrayManager<E, EArray> arrayManager) {
        this.arrayManager = arrayManager;
        this.clear();
    }
    protected CustomIndexCppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size) {
        this(arrayManager);
        this.resize(size);
    }
    protected CustomIndexCppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size, E value) {
        this(arrayManager);
        this.resize(size, value);
    }
    protected CustomIndexCppVectorImpl(DataArrayManager<E, EArray> arrayManager, EArray array) {
        this(arrayManager);
        this.array = array;
    }
    protected CustomIndexCppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size, Supplier<E> value) {
        this(arrayManager);
        this.resize(size, value);
    }
    protected CustomIndexCppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size, Function<I, E> value) {
        this(arrayManager);
        this.resize(size, value);
    }

    @Override
    @Nonnull
    public java.util.Iterator<E> iterator() {
        return new ListIterator(integerToIndex(0));
    }

    public void clear() {
        array = arrayManager.newArray(0);
        size = 0;
    }

    public void assign(int count, E value) {
        this.clear();
        for(int i = 0; i < count; i++) this.pushBack(value);
    }
    public void assign(Function<I, E> values, I first, I last) {
        this.clear();
        Iterator<I> iterator = this.newIterator(first, last);
        while(iterator.hasNext()) this.pushBack(values.apply(iterator.next()));
    }

    public E front() {
        return arrayManager.get(array, 0);
    }
    public E back() {
        return arrayManager.get(array, size - 1);
    }
    public E get(I index) {
        return arrayManager.get(array, getIndexValue(index));
    }
    public E set(I index, E value) {
        E oldValue = arrayManager.get(array, getIndexValue(index));
        arrayManager.set(array, getIndexValue(index), value);
        return oldValue;
    }

    public void reserve(int minCapacity) {
        int oldCapacity = arrayManager.length(array);
        if(oldCapacity < minCapacity) {
            int newCapacity = oldCapacity + oldCapacity >> 1;
            if(newCapacity < minCapacity) newCapacity = minCapacity;
            EArray newArray = arrayManager.newArray(newCapacity);
            arrayManager.copy(array, 0, newArray, 0, this.size);
            array = newArray;
        }
    }

    // From C++ reference:
    // - Resizes the container so that it contains n elements.
    // - If n is smaller than the current container size, the content is reduced to
    //   its first n elements, removing those beyond.
    // - If n is greater than the current container size, the content is expanded by
    //   inserting at the end as many elements as needed to reach a size of n.
    //   If val is specified, the new elements are initialized as copies of val,
    //   otherwise, they are value-initialized.
    // - If n is also greater than the current container capacity, an automatic
    //   reallocation of the allocated storage space takes place.
    public void resize(int size) {
        this.resize(size, i -> null);
    }
    public void resize(int size, E value) {
        this.resize(size, i -> value);
    }
    public void resize(int size, Supplier<E> value) {
        this.resize(size, i -> value.get());
    }
    public void resize(int size, Function<I, E> value) {
        if(size > this.size) {
            this.reserve(size);
            for(int i = this.size; i < size; i++) {
                arrayManager.set(array, i, value.apply(integerToIndex(i)));
            }
        }
        else if(size < this.size) {
            // Set the rest of the array to null
            for(int i = size; i < this.size; i++) {
                arrayManager.set(array, i, null);
            }
        }
        this.size = size;
    }

    public void insert(I index, E value) {
        this.reserve(this.size + 1);
        int indexValue = getIndexValue(index);
        arrayManager.copy(array, indexValue, array, indexValue + 1, this.size - indexValue);
        arrayManager.set(array, indexValue, value);
        this.size++;
    }

    public void sort(@Nullable Comparator<E> comparator) {
        arrayManager.sort(array, 0, this.size, comparator);
    }

    // From C++ reference:
    // - Removes from the vector either a single element.
    public void erase(I index) {
        int indexValue = getIndexValue(index);
        arrayManager.copy(array, indexValue + 1, array, indexValue, this.size - indexValue - 1);
        arrayManager.set(array, this.size - 1, null);
        this.size--;
    }

    public int size() { return this.size; }
    public boolean isEmpty() { return this.size == 0; }

    @Override
    public void swap(V other) {
        if(other instanceof CustomIndexCppVectorImpl) {
            CustomIndexCppVectorImpl<I, E, EArray, V> otherImpl = BTRUtil.uncheckedCast(other);
            EArray tempArray = this.array;
            int tempSize = this.size;
            this.array = otherImpl.array;
            this.size = otherImpl.size;
            otherImpl.array = tempArray;
            otherImpl.size = tempSize;
        }
        else {
            int tempSize = other.size();
            EArray tempArray = arrayManager.newArray(tempSize);
            for(int i = 0; i < tempSize; i++) {
                arrayManager.set(tempArray, i, other.get(integerToIndex(i)));
            }
            other.resize(this.size, this::get);
            this.array = tempArray;
            this.size = tempSize;
        }
    }

    public void pushBack(E value) {
        this.reserve(this.size + 1);
        arrayManager.set(array, this.size, value);
        this.size++;
    }
    @SuppressWarnings("UnusedReturnValue")
    public E popBack() {
        E oldValue = arrayManager.get(array, this.size - 1);
        arrayManager.set(array, this.size - 1, null);
        this.size--;
        return oldValue;
    }

    public boolean contains(Object element) {
        for(int i = 0; i < this.size; i++) {
            if(Objects.equals(arrayManager.get(array, i), element)) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomIndexCppVectorImpl<?, ?, ?, ?> that = (CustomIndexCppVectorImpl<?, ?, ?, ?>) o;
        // Check size, array type, array content
        if(!array.getClass().equals(that.array.getClass())) return false;
        EArray thatArray = BTRUtil.uncheckedCast(that.array);
        return size == that.size && arrayManager.arrayEquals(array, thatArray);
    }

    @Override
    public int hashCode() {
        return arrayManager.arrayHashCode(array);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append("[");
        for(int i = 0; i < this.size; i++) {
            if(i != 0) sb.append(", ");
            sb.append(arrayManager.get(array, i));
        }
        sb.append("]");
        return sb.toString();
    }

    protected abstract int getIndexValue(I index);
    protected abstract I integerToIndex(int index);
    protected abstract Iterator<I> newIterator(I start, I end);
    protected abstract I nextIndex(I index);
    protected abstract I previousIndex(I index);

    @AllArgsConstructor
    private class ListIterator implements java.util.ListIterator<E> {
        private I index;

        @Override public boolean hasNext() { return getIndexValue(index) < size; }
        @Override public E next() { return get(index); }
        @Override public boolean hasPrevious() { return getIndexValue(index) > 0; }
        @Override public E previous() { return get(CustomIndexCppVectorImpl.this.previousIndex(index)); }
        @Override public int nextIndex() { return getIndexValue(CustomIndexCppVectorImpl.this.nextIndex(index)); }
        @Override public int previousIndex() { return getIndexValue(CustomIndexCppVectorImpl.this.previousIndex(index)); }
        @Override public void remove() { erase(index); }
        @Override public void set(E e) { CustomIndexCppVectorImpl.this.set(index, e); }
        @Override public void add(E e) { CustomIndexCppVectorImpl.this.insert(index, e); }
    }
}
