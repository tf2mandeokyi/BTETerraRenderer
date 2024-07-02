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
public abstract class IndexTypeImpl<I extends IndexTypeImpl<I>> implements IndexType<I> {

    private final int value;

    public I add(int other) {
        return this.newInstance(this.value + other);
    }
    public I subtract(int other) {
        return this.newInstance(this.value - other);
    }
    public Iterator<I> until(I end) {
        return new IndexTypeIterator(this.value, end.getValue());
    }

    protected abstract I newInstance(int value);
    public abstract boolean isInvalid();

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + value + ")";
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }

    public boolean equals(I other) {
        return value == other.getValue();
    }

    @Override
    public boolean equals(Object obj) {
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
    protected interface IndexArrayManager<I extends IndexTypeImpl<I>> extends DataArrayManager<I, int[]> {

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
