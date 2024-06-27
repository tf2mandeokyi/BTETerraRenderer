package com.mndk.bteterrarenderer.draco.core.vector;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

class CppVectorImpl<E, EArray> extends CustomIndexCppVectorImpl<Integer, E, EArray> implements CppVector<E> {

    CppVectorImpl(DataArrayManager<E, EArray> arrayManager) {
        super(arrayManager);
    }
    CppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size) {
        super(arrayManager, size);
    }
    CppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size, E value) {
        super(arrayManager, size, value);
    }
    CppVectorImpl(DataArrayManager<E, EArray> arrayManager, EArray array) {
        super(arrayManager, array);
    }
    CppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size, Supplier<E> value) {
        super(arrayManager, size, value);
    }
    CppVectorImpl(DataArrayManager<E, EArray> arrayManager, int size, Function<Integer, E> value) {
        super(arrayManager, size, value);
    }

    @Override protected int getIndexValue(Integer index) { return index; }
    @Override protected Integer integerToIndex(int index) { return index; }
    @Override protected Iterator<Integer> newIterator(Integer start, Integer end) { return IntStream.range(start, end).iterator(); }
    @Override protected Integer nextIndex(Integer index) { return index + 1; }
    @Override protected Integer previousIndex(Integer index) { return index - 1; }

}
