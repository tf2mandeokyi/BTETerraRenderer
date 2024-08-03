package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class NullPointer<T> implements Pointer<T> {
    private static void fail() {
        throw new UnsupportedOperationException("Cannot modify value from a null address");
    }

    @Override public DataType<T> getType() { return DataType.object(() -> null); }
    @Override public T get() { return null; }
    @Override public T get(long index) { return null; }
    @Override public void set(T value) { fail(); }
    @Override public void set(long index, T value) { fail(); }
    @Override public Pointer<T> add(long offset) { return this; }
    @Override public RawPointer asRaw() { throw new UnsupportedOperationException(); }
}
