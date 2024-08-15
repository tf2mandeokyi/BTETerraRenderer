package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

@Getter
@RequiredArgsConstructor
class BorrowedTypeArray<T> extends BorrowedArray<T> implements Pointer<T> {
    private final DataType<T> type;
    private final T[] array;
    private final int offset;

    @Override public DataType<T> getType() { return type; }
    @Override public T get() {
        return array[offset] != null ? array[offset] : (array[offset] = type.defaultValue());
    }
    @Override public void set(T value) { array[offset] = value; }
    @Override public Pointer<T> add(int offset) { return new BorrowedTypeArray<>(type, array, this.offset + offset); }
    @Override public RawPointer asRaw() { throw new UnsupportedOperationException(); }
    @Override protected T get(int index) {
        return array[offset + index] != null ? array[offset + index] : (array[offset + index] = type.defaultValue());
    }
    @Override protected void set(int index, T value) { array[offset + index] = value; }
    @Override protected void swap(int a, int b) {
        T temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }
}
