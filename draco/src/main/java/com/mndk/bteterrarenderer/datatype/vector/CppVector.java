package com.mndk.bteterrarenderer.datatype.vector;

import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CppVector<E> implements Iterable<E> {

    private long size = 0, capacity = 0;
    private Pointer<E> array;
    private final DataType<E> type;

    public CppVector(DataType<E> type) {
        this.type = type;
        this.clear();
    }
    public CppVector(DataType<E> type, long size) {
        this(type);
        this.resize(size);
    }
    public CppVector(DataType<E> type, long size, E value) {
        this(type);
        this.resize(size, value);
    }

    public CppVector(Supplier<E> defaultValueMaker) {
        this(DataType.object(defaultValueMaker));
    }
    public CppVector(Supplier<E> defaultValueMaker, long size) {
        this(DataType.object(defaultValueMaker), size);
    }

    public void clear() {
        this.array = type.newArray(0);
        this.size = 0;
        this.capacity = 0;
    }

    // From C++ reference:
    // - Assigns new contents to the vector, replacing its current contents, and
    //   modifying its size accordingly.
    public void assign(long count, E value) {
        this.clear();
        this.reserve(count);
        for (long i = 0; i < count; i++) this.pushBack(value);
    }
    public void assign(Pointer<E> value, long count) {
        this.clear();
        this.reserve(count);
        for (long i = 0; i < count; i++) this.pushBack(value.get(i));
    }

    public long size() { return this.size; }
    public boolean isEmpty() { return this.size == 0; }

    private void checkNotEmpty() {
        if (this.size != 0) return;
        throw new IndexOutOfBoundsException("Vector is empty");
    }
    private void checkIndex(long index) {
        if (index >= 0 && index < size) return;
        throw new IndexOutOfBoundsException("Index " + index + " is out of bounds (size = " + size + ")");
    }

    public E front() { checkNotEmpty(); return array.get(0); }
    public E back() { checkNotEmpty(); return array.get(size - 1); }
    public E get(UInt index) { return this.get(index.intValue()); }
    public E get(long index) { checkIndex(index); return array.get(index); }
    public boolean contains(E element) {
        for (long i = 0; i < this.size; i++) {
            if (Objects.equals(array.get(i), element)) return true;
        }
        return false;
    }

    public E set(UInt index, E value) { return this.set(index.intValue(), value); }
    public E set(long index, E value) {
        checkIndex(index);
        E oldValue = array.get(index);
        array.set(index, value);
        return oldValue;
    }
    public E set(UInt index, Function<E, E> function) { return this.set(index.intValue(), function); }
    public E set(long index, Function<E, E> function) {
        return this.set(index, function.apply(this.get(index)));
    }
    public void insert(long index, E value) {
        this.reserve(this.size + 1);
        PointerHelper.copyMultiple(array.add(index), array.add(index + 1), this.size - index);
        array.set(index, value);
        this.size++;
    }
    public void insert(long index, Pointer<E> data, long count) {
        this.reserve(this.size + count);
        PointerHelper.copyMultiple(array.add(index), array.add(index + count), this.size - index);
        PointerHelper.copyMultiple(data, array.add(index), count);
        this.size += count;
    }
    public void pushBack(E value) {
        this.reserve(this.size + 1);
        array.set(this.size, value);
        this.size++;
    }
    public void erase(long index) {
        PointerHelper.copyMultiple(array.add(index + 1), array.add(index), this.size - index - 1);
        array.reset(this.size - 1);
        this.size--;
    }
    public E popBack() {
        E oldValue = array.get(this.size - 1);
        array.reset(this.size - 1);
        this.size--;
        return oldValue;
    }

    public void swap(CppVector<E> other) {
        if (other instanceof CppVector) {
            Pointer<E> tempArray = this.array;
            long tempSize = this.size;
            long tempCapacity = this.capacity;
            this.array = other.array;
            this.size = other.size;
            this.capacity = other.capacity;
            other.array = tempArray;
            other.size = tempSize;
            other.capacity = tempCapacity;
        }
    }

    public void sort(@Nullable Comparator<E> comparator) { PointerHelper.sortContent(array, size, comparator); }
    public boolean isSorted(@Nullable Comparator<E> comparator) { return PointerHelper.isContentSorted(array, size, comparator); }
    public void reverse() { PointerHelper.reverse(array, size); }
    public Pointer<E> getPointer() { return array; }
    public RawPointer getRawPointer() { return array.asRaw(); }
    public Stream<E> stream() { return PointerHelper.stream(array, size); }
    @Override @Nonnull public java.util.Iterator<E> iterator() { return PointerHelper.iterator(array, size); }

    // From C++ reference:
    // - Requests that the vector capacity be at least enough to contain n elements.
    // - If n is greater than the current vector capacity, the function causes the
    //   container to reallocate its storage increasing its capacity to n (or greater).
    // - In all other cases, the function call does not cause a reallocation and the
    //   vector capacity is not affected.
    // - This function has no effect on the vector size and cannot alter its elements.
    public void reserve(long minCapacity) {
        long oldCapacity = capacity;
        if (oldCapacity < minCapacity) {
            long newCapacity = oldCapacity + oldCapacity >> 1;
            if (newCapacity < minCapacity) newCapacity = minCapacity;
            Pointer<E> newArray = type.newArray(newCapacity);
            PointerHelper.copyMultiple(array, newArray, size);
            array = newArray;
            capacity = newCapacity;
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
    public void resize(long size) { this.resize(size, i -> type.defaultValue()); }
    public void resize(long size, E value) { this.resize(size, i -> value); }
    public void resize(long size, Supplier<E> value) { this.resize(size, i -> value.get()); }
    public void resize(long size, Function<Long, E> value) {
        if (size > this.size) {
            this.reserve(size);
            for (long i = this.size; i < size; i++) {
                array.set(i, value.apply(i));
            }
        }
        else if (size < this.size) {
            // Set the rest of the array to default values
            for (long i = size; i < this.size; i++) {
                array.reset(i);
            }
        }
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CppVector<?> that = (CppVector<?>) o;
        // Check size, array type, array content
        if (!array.getClass().equals(that.array.getClass())) return false;
        Pointer<E> thatArray = BTRUtil.uncheckedCast(that.array);
        return size == that.size && PointerHelper.contentEquals(array, thatArray, size);
    }

    @Override
    public int hashCode() {
        return PointerHelper.contentHashCode(array, size);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[size = ").append(this.size);
        if (this.size > 0) {
            sb.append("; ");
            for (long i = 0; i < this.size; i++) {
                if (i != 0) sb.append(", ");
                sb.append(array.get(i));
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
