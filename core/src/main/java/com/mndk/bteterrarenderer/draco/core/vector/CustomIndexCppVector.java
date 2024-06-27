package com.mndk.bteterrarenderer.draco.core.vector;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

interface CustomIndexCppVector<I, E> extends Iterable<E> {
    // From C++ reference:
    // - Assigns new contents to the vector, replacing its current contents, and
    //   modifying its size accordingly.
    void assign(int count, E value);
    void assign(Function<I, E> values, I start, I end);

    int size();
    default boolean isEmpty() { return this.size() == 0; }

    default Function<I, E> getter() { return this::get; }
    default BiConsumer<I, E> setter() { return this::set; }

    E front();
    E back();
    E get(I index);
    boolean contains(E value);

    E set(I index, E value);
    void insert(I index, E value);
    void pushBack(E value);
    void clear();
    void erase(I index);
    E popBack();
    void swap(CustomIndexCppVector<I, E> other);
    void sort(@Nullable Comparator<E> comparator);

    // From C++ reference:
    // - Requests that the vector capacity be at least enough to contain n elements.
    // - If n is greater than the current vector capacity, the function causes the
    //   container to reallocate its storage increasing its capacity to n (or greater).
    // - In all other cases, the function call does not cause a reallocation and the
    //   vector capacity is not affected.
    // - This function has no effect on the vector size and cannot alter its elements.
    void reserve(int minCapacity);

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
    void resize(int size);
    void resize(int size, E value);
    void resize(int size, Supplier<E> value);
    void resize(int size, Function<I, E > values);

}
