package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;

@Getter
@RequiredArgsConstructor
class BorrowedObjectArray<T> extends BorrowedArray<T> implements Pointer<T> {
    private final DataType<T> type;
    private final Object[] array;
    private final int offset;

    @Override public DataType<T> getType() { return type; }
    @Override public T get() {
        Object result = array[offset] != null ? array[offset] : (array[offset] = type.defaultValue());
        return BTRUtil.uncheckedCast(result);
    }
    @Override public T get(int index) {
        Object result = array[offset + index] != null ? array[offset + index] : (array[offset + index] = type.defaultValue());
        return BTRUtil.uncheckedCast(result);
    }
    @Override public void set(T value) { array[offset] = value; }
    @Override public void set(int index, T value) { array[offset + index] = value; }
    @Override public Pointer<T> add(int offset) { return new BorrowedObjectArray<>(type, array, this.offset + offset); }
    @Override public RawPointer asRaw() { throw new UnsupportedOperationException(); }
    @Override public void swap(int a, int b) {
        Object temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }
    @Override public void sort(int length, @Nullable Comparator<T> comparator) {
        Comparator<Object> objectComparator = comparator == null ? null
                : (a, b) -> comparator.compare(BTRUtil.uncheckedCast(a), BTRUtil.uncheckedCast(b));
        Arrays.sort(array, this.offset, this.offset + length, objectComparator);
    }
}
