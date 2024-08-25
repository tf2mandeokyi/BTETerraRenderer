package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.pointer.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Iterator;

@Getter
@AllArgsConstructor
public abstract class IndexTypeImpl<I extends IndexTypeImpl<I>> implements IndexType<I> {

    private final int value;

    public final I add(int other) {
        return this.newInstance(this.value + other);
    }
    public final I subtract(int other) {
        return this.newInstance(this.value - other);
    }
    public final Iterator<I> until(I end) {
        return new IndexTypeIterator(this.value, end.getValue());
    }

    protected abstract I newInstance(int value);
    public abstract boolean isInvalid();

    @Override public final String toString() { return String.valueOf(value); }
    @Override public final int hashCode() { return Integer.hashCode(value); }

    public final boolean equals(int other) { return value == other; }
    public final boolean equals(I other) { return value == other.getValue(); }

    @Override
    public final boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof Integer) {
            return value == (Integer) obj;
        }
        if(obj instanceof IndexTypeImpl<?>) {
            return value == ((IndexTypeImpl<?>) obj).value;
        }
        return false;
    }

    private class IndexTypeIterator implements Iterator<I> {
        private final int until;
        private int current;
        public IndexTypeIterator(int start, int until) {
            this.until = until;
            current = start;
        }
        @Override public boolean hasNext() { return current < until; }
        @Override public I next() { return newInstance(current++); }
    }

    @FunctionalInterface
    protected interface IndexTypeManager<I extends IndexTypeImpl<I>> extends DataType<I> {

        I intToIndex(int value);

        @Override default I parse(String value) { return intToIndex(Integer.parseInt(value)); }
        @Override default I defaultValue() { return intToIndex(0); }
        @Override default boolean equals(I left, I right) { return left.equals(right); }
        @Override default int hashCode(I value) { return value.hashCode(); }
        @Override default String toString(I value) { return value.toString(); }

        @Override default long byteSize() { return 4; }
        @Override default I read(RawPointer src) {
            return intToIndex(src.getRawInt());
        }
        @Override default void write(RawPointer dst, I value) {
            dst.setRawInt(value.getValue());
        }

        @Override default Pointer<I> newOwned(I value) {
            return Pointer.newObject(this, value);
        }
        @Override default Pointer<I> newArray(int length) {
            return Pointer.newObjectArray(this, length);
        }
        @Override default Pointer<I> castPointer(RawPointer pointer) {
            throw new UnsupportedOperationException("Cannot cast pointer from " + pointer.getClass().getSimpleName()
                    + " to " + this.getClass().getSimpleName());
        }
    }
}
