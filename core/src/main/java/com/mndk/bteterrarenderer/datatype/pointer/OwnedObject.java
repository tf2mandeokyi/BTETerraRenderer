package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class OwnedObject<T> extends SingleVariablePointer<T> implements Pointer<T> {
    private final DataType<T> type;
    private T value;

    @Override public DataType<T> getType() { return type; }
    @Override public T get() { return value; }
    @Override public T get(long index) { checkIndex(index); return value; }
    @Override public void set(T value) { this.value = value; }
    @Override public void set(long index, T value) { checkIndex(index); this.value = value; }
    @Override public RawPointer asRaw() { throw new UnsupportedOperationException(); }
}
