package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ObjectType<T> implements DataType<T> {

    private final Supplier<T> defaultValueMaker;

    // Java overrides
    @Override public String toString() { return "object"; }

    // IO operations
    @Override public long byteSize() { throw new UnsupportedOperationException(); }
    @Override public T read(RawPointer src) {
        throw new UnsupportedOperationException();
    }
    @Override public void write(RawPointer dst, T value) {
        throw new UnsupportedOperationException();
    }

    // General conversions
    @Override public T parse(String value) { throw new UnsupportedOperationException(); }
    @Override public T defaultValue() { return defaultValueMaker.get(); }
    @Override public boolean equals(T left, T right) { return left.equals(right); }
    @Override public int hashCode(T value) { return value.hashCode(); }
    @Override public String toString(T value) { return value.toString(); }

    // Pointer operations
    @Override public Pointer<T> newOwned(T value) { return Pointer.newObject(this, value); }
    @Override public Pointer<T> newArray(int length) { return Pointer.wrap(this, new Object[length], 0); }
    @Override public Pointer<T> castPointer(RawPointer pointer) {
        throw new UnsupportedOperationException();
    }
}
