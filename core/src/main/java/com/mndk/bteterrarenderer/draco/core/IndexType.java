package com.mndk.bteterrarenderer.draco.core;

import java.util.Iterator;

public interface IndexType<I extends IndexType<I>> extends Comparable<I> {

    int getValue();
    I add(int other);
    I subtract(int other);
    boolean isInvalid();
    Iterator<I> until(I end);

    default I add(I other) { return this.add(other.getValue()); }
    default I subtract(I other) { return this.subtract(other.getValue()); }
    default I increment() { return this.add(1); }
    default boolean isValid() { return !isInvalid(); }

    @Override default int compareTo(I o) {
        return Integer.compare(getValue(), o.getValue());
    }
}
