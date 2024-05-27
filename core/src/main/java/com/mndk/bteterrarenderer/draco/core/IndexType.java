package com.mndk.bteterrarenderer.draco.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IndexType extends Number implements Comparable<IndexType> {

    private int value;

    public IndexType(IndexType value) {
        this.value = value.value;
    }

    public IndexType add(IndexType other) {
        return new IndexType(this.value + other.value);
    }

    public IndexType selfAdd(IndexType other) {
        this.value += other.value;
        return this;
    }

    public IndexType selfIncrement() {
        this.value++;
        return this;
    }

    public IndexType subtract(IndexType other) {
        return new IndexType(this.value - other.value);
    }

    public IndexType selfSubtract(IndexType other) {
        this.value -= other.value;
        return this;
    }

    public IndexType selfDecrement() {
        this.value--;
        return this;
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
        if(obj instanceof IndexType) {
            return value == ((IndexType) obj).value;
        }
        return false;
    }

    @Override public int compareTo(IndexType o) {
        return Integer.compare(value, o.value);
    }
    @Override public int intValue() { return this.value; }
    @Override public long longValue() { return this.value; }
    @Override public float floatValue() { return this.value; }
    @Override public double doubleValue() { return this.value; }
}
