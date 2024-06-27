package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.mintern.primitive.Primitive;
import net.mintern.primitive.comparators.IntComparator;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

@Getter
@AllArgsConstructor
public abstract class IndexType<I extends IndexType<I>> extends Number implements Comparable<I> {

    private final int value;

    public I add(I other) {
        return this.newInstance(this.value + other.getValue());
    }
    public I add(int other) {
        return this.newInstance(this.value + other);
    }

    public I subtract(I other) {
        return this.newInstance(this.value - other.getValue());
    }
    public I subtract(int other) {
        return this.newInstance(this.value - other);
    }

    public I increment() {
        return this.newInstance(this.value + 1);
    }

    public Iterator<I> until(I end) {
        return new IndexTypeIterator(this.value, end.getValue());
    }

    protected abstract I newInstance(int value);
    public abstract boolean isInvalid();
    public boolean isValid() {
        return !isInvalid();
    }

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
        private final int until;
        private int current;

        public IndexTypeIterator(int start, int until) {
            this.until = until;
            current = start;
        }

        @Override
        public boolean hasNext() {
            return current < until;
        }

        @Override
        public I next() {
            return newInstance(current++);
        }
    }

    @FunctionalInterface
    protected interface IndexArrayManager<I extends IndexType<I>> extends DataArrayManager<I, int[]> {

        I intToIndex(int value);

        default int[] newArray(int length) { return new int[length]; }
        default I get(int[] array, int index) { return intToIndex(array[index]); }
        default void set(int[] array, int index, @Nullable I value) {
            array[index] = value == null ? 0 : value.getValue();
        }
        default int length(int[] array) { return array.length; }
        default void copy(int[] src, int srcIndex, int[] dest, int destIndex, int length) {
            System.arraycopy(src, srcIndex, dest, destIndex, length);
        }
        default void sort(int[] array, int from, int to, @Nullable Comparator<I> comparator) {
            IntComparator c = comparator == null ? null : (a, b) -> comparator.compare(intToIndex(a), intToIndex(b));
            Primitive.sort(array, from, to, c);
        }
        default int arrayHashCode(int[] array) { return Arrays.hashCode(array); }
        default boolean arrayEquals(int[] array1, int[] array2) { return Arrays.equals(array1, array2); }
    }
}
