package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.util.BTRUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class BorrowedObjectArray<T> extends BorrowedArray<T> implements Pointer<T> {
    private final DataType<T> type;
    private final Object[] array;
    private final int offset;

    @Override public DataType<T> getType() { return type; }
    @Override public T get() {
        return BTRUtil.uncheckedCast(array[offset] != null
                ? array[offset] : (array[offset] = type.defaultValue()));
    }
    @Override public void set(T value) { array[offset] = value; }
    @Override public RawPointer asRaw() { throw new UnsupportedOperationException(); }
    @Override protected T get(int index) {
        return BTRUtil.uncheckedCast(array[offset + index] != null
                ? array[offset + index] : (array[offset + index] = type.defaultValue()));
    }
    @Override protected void set(int index, T value) { array[offset + index] = value; }
    @Override protected Pointer<T> add(int offset) { return new BorrowedObjectArray<>(type, array, this.offset + offset); }
    @Override protected void swap(int a, int b) {
        Object temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }
}
