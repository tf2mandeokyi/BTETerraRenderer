package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
class FunctionalPointer<T> extends SingleVariablePointer<T> {
    private final DataType<T> type;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    @Override public DataType<T> getType() { return type; }
    @Override public T get() { return getter.get(); }
    @Override public T get(long index) { checkIndex(index); return getter.get(); }
    @Override public void set(T value) { setter.accept(value); }
    @Override public void set(long index, T value) { checkIndex(index); setter.accept(value); }
    @Override public RawPointer asRaw() { throw new UnsupportedOperationException(); }
}
