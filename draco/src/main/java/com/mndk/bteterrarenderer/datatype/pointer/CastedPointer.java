package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
abstract class CastedPointer<E> implements RawPointer, Pointer<E> {
    protected final RawPointer pointer;
    protected final long offset;

    @Override public final Object getOrigin() { return pointer.getOrigin(); }
    @Override public final RawPointer asRaw() { return this; }

    @Override public final String toString() {
        return "cast<" + this.getType().toString() + ">(" + pointer.toString() + " + " + offset + ")";
    }

    @Override public final int hashCode() {
        return Objects.hash(pointer, offset);
    }

    @Override public final boolean equals(Object obj) {
        // Compare pointer, offset, and the data type
        if (!(obj instanceof CastedPointer)) return false;
        CastedPointer<?> other = (CastedPointer<?>) obj;
        return pointer.equals(other.pointer) && offset == other.offset && this.getType().equals(other.getType());
    }
}
