package com.mndk.bteterrarenderer.datatype;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface DataArrayManager<T, TArray> {
    // Array operations
    TArray newArray(int length);
    T get(TArray array, int index);
    void set(TArray array, int index, @Nullable T value);
    int length(TArray array);
    void copy(TArray src, int srcIndex, TArray dest, int destIndex, int length);
    void sort(TArray array, int from, int to, @Nullable Comparator<T> comparator);
    int arrayHashCode(TArray array);
    boolean arrayEquals(TArray array1, TArray array2);
    default List<T> asList(TArray array) {
        int length = this.length(array);
        List<T> list = new ArrayList<>(length);
        for(int i = 0; i < length; ++i) list.add(this.get(array, i));
        return list;
    }
    default Function<Integer, T> getter(TArray array) {
        return index -> this.get(array, index);
    }
    default BiConsumer<Integer, T> setter(TArray array) {
        return (index, value) -> this.set(array, index, value);
    }
}
