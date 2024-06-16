package com.mndk.bteterrarenderer.draco.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Iterator;

@Getter
@AllArgsConstructor
public abstract class IndexType<I extends IndexType<I>> extends Number implements Comparable<I> {

    private int value;

    public I add(I other) {
        return this.newInstance(this.value + other.getValue());
    }

    public I subtract(I other) {
        return this.newInstance(this.value - other.getValue());
    }

    public I next() {
        return this.newInstance(this.value + 1);
    }

    public Iterator<I> until(I end) {
        return new IndexTypeIterator(this.value, end.getValue());
    }

    protected abstract I newInstance(int value);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ")";
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof Integer) {
            return value == (Integer) obj;
        }
        if(obj instanceof IndexType<?>) {
            return value == ((IndexType<?>) obj).value;
        }
        return false;
    }

    @Override public int compareTo(I o) {
        return Integer.compare(value, o.getValue());
    }
    @Override public int intValue() { return this.value; }
    @Override public long longValue() { return this.value; }
    @Override public float floatValue() { return this.value; }
    @Override public double doubleValue() { return this.value; }

    private class IndexTypeIterator implements Iterator<I> {
        private final int end;
        private int current;

        public IndexTypeIterator(int start, int end) {
            this.end = end;
            current = start;
        }

        @Override
        public boolean hasNext() {
            return current < end;
        }

        @Override
        public I next() {
            return newInstance(current++);
        }
    }
}
