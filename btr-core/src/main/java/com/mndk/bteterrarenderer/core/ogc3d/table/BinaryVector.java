package com.mndk.bteterrarenderer.core.ogc3d.table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@Getter
/*
 * TODO: Separating "VecN"s shouldn't be a good idea. Try Number[] instead
 */
public abstract class BinaryVector<T> {
    protected final T[] elements;

    @SafeVarargs
    public BinaryVector(T... elements) {
        this.elements = elements;
    }

    @EqualsAndHashCode(callSuper = true)
    public static class Vec2<T> extends BinaryVector<T> {
        @SafeVarargs public Vec2(T... elements) { super(elements); }
    }
    @EqualsAndHashCode(callSuper = true)
    public static class Vec3<T> extends BinaryVector<T> {
        @SafeVarargs public Vec3(T... elements) { super(elements); }
    }
    @EqualsAndHashCode(callSuper = true)
    public static class Vec4<T> extends BinaryVector<T> {
        @SafeVarargs public Vec4(T... elements) { super(elements); }
    }
}
