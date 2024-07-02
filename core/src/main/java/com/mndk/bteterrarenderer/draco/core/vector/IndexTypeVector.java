package com.mndk.bteterrarenderer.draco.core.vector;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.IndexType;

import java.util.function.Function;

public interface IndexTypeVector<I extends IndexType<I>, E>
        extends CustomIndexCppVector<I, E, IndexTypeVector<I, E>> {

    static <I extends IndexType<I>, E>
    IndexTypeVector<I, E> create(Function<Integer, I> indexConstructor) {
        return new IndexTypeVectorImpl<>(indexConstructor, DataType.object());
    }
    static <I extends IndexType<I>, E>
    IndexTypeVector<I, E> create(Function<Integer, I> indexConstructor, int size) {
        return new IndexTypeVectorImpl<>(indexConstructor, DataType.object(), size);
    }
    static <I extends IndexType<I>, E>
    IndexTypeVector<I, E> create(Function<Integer, I> indexConstructor, int size, E value) {
        return new IndexTypeVectorImpl<>(indexConstructor, DataType.object(), size, value);
    }

    static <I extends IndexType<I>, E, EArray>
    IndexTypeVector<I, E> create(Function<Integer, I> indexConstructor,
                                 DataArrayManager<E, EArray> arrayManager) {
        return new IndexTypeVectorImpl<>(indexConstructor, arrayManager);
    }
    static <I extends IndexType<I>, E, EArray>
    IndexTypeVector<I, E> create(Function<Integer, I> indexConstructor,
                                 DataArrayManager<E, EArray> arrayManager, EArray array) {
        return new IndexTypeVectorImpl<>(indexConstructor, arrayManager, array);
    }
    static <I extends IndexType<I>, E, EArray>
    IndexTypeVector<I, E> create(Function<Integer, I> indexConstructor,
                                 DataArrayManager<E, EArray> arrayManager, int size) {
        return new IndexTypeVectorImpl<>(indexConstructor, arrayManager, size);
    }
    static <I extends IndexType<I>, E, EArray>
    IndexTypeVector<I, E> create(Function<Integer, I> indexConstructor,
                                 DataArrayManager<E, EArray> arrayManager, int size, E value) {
        return new IndexTypeVectorImpl<>(indexConstructor, arrayManager, size, value);
    }

    default IndexTypeVector<I, E> withOffset(I offset) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
