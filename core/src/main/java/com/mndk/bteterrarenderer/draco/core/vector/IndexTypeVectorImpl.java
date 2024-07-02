package com.mndk.bteterrarenderer.draco.core.vector;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import com.mndk.bteterrarenderer.draco.core.IndexType;

import java.util.Iterator;
import java.util.function.Function;

class IndexTypeVectorImpl<I extends IndexType<I>, E, EArray>
        extends CustomIndexCppVectorImpl<I, E, EArray, IndexTypeVector<I, E>>
        implements IndexTypeVector<I, E> {

    private final Function<Integer, I> indexConstructor;

    IndexTypeVectorImpl(Function<Integer, I> indexConstructor, DataArrayManager<E, EArray> arrayManager) {
        super(arrayManager);
        this.indexConstructor = indexConstructor;
    }
    IndexTypeVectorImpl(Function<Integer, I> indexConstructor, DataArrayManager<E, EArray> arrayManager, EArray array) {
        super(arrayManager, array);
        this.indexConstructor = indexConstructor;
    }
    IndexTypeVectorImpl(Function<Integer, I> indexConstructor, DataArrayManager<E, EArray> arrayManager, int size) {
        super(arrayManager, size);
        this.indexConstructor = indexConstructor;
    }
    IndexTypeVectorImpl(Function<Integer, I> indexConstructor, DataArrayManager<E, EArray> arrayManager, int size, E value) {
        super(arrayManager, size, value);
        this.indexConstructor = indexConstructor;
    }

    @Override protected int getIndexValue(I index) { return index.getValue(); }
    @Override protected I integerToIndex(int index) { return indexConstructor.apply(index); }
    @Override protected Iterator<I> newIterator(I start, I end) { return start.until(end); }
    @Override protected I nextIndex(I index) { return index.add(1); }
    @Override protected I previousIndex(I index) { return index.subtract(1); }
}
