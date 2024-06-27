package com.mndk.bteterrarenderer.draco.core.vector;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UInt;

import java.util.function.Function;
import java.util.function.Supplier;

public interface CppVector<E> extends CustomIndexCppVector<Integer, E> {

    static <E> CppVector<E> create() {
        return new CppVectorImpl<>(DataType.object());
    }
    static <E> CppVector<E> create(int size) {
        return new CppVectorImpl<>(DataType.object(), size);
    }
    static <E> CppVector<E> create(int size, E value) {
        return new CppVectorImpl<>(DataType.object(), size, value);
    }
    static <E> CppVector<E> create(int size, Supplier<E> value) {
        return new CppVectorImpl<E, Object[]>(DataType.object(), size, value);
    }
    static <E> CppVector<E> create(int size, Function<Integer, E> value) {
        return new CppVectorImpl<E, Object[]>(DataType.object(), size, value);
    }

    static <E> CppVector<E> create(DataArrayManager<E, ?> arrayManager) {
        return new CppVectorImpl<>(arrayManager);
    }
    static <E> CppVector<E> create(DataArrayManager<E, ?> arrayManager, int size) {
        return new CppVectorImpl<>(arrayManager, size);
    }
    static <E> CppVector<E> create(DataArrayManager<E, ?> arrayManager, int size, E value) {
        return new CppVectorImpl<>(arrayManager, size, value);
    }
    static <E, EArray> CppVector<E> create(DataArrayManager<E, EArray> arrayManager, EArray array) {
        return new CppVectorImpl<>(arrayManager, array);
    }
    static <E> CppVector<E> create(DataArrayManager<E, ?> arrayManager, int size, Supplier<E> value) {
        return new CppVectorImpl<>(arrayManager, size, value);
    }
    static <E> CppVector<E> create(DataArrayManager<E, ?> arrayManager, int size, Function<Integer, E> value) {
        return new CppVectorImpl<>(arrayManager, size, value);
    }

    default E get(UInt index) { return get(index.intValue()); }
    default void set(UInt index, E value) { set(index.intValue(), value); }

}
