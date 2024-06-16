package com.mndk.bteterrarenderer.draco.core;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.function.Function;

@RequiredArgsConstructor
public class IndexTypeVector<I extends IndexType<I>, E> extends CppVectorInternal<I, E> {

    private final Function<Integer, I> indexConstructor;

    @Override
    protected int getIndexValue(I index) {
        return index.getValue();
    }

    @Override
    protected I integerToIndex(int index) {
        return indexConstructor.apply(index);
    }

    @Override
    protected Iterator<I> newIterator(I start, I end) {
        return start.until(end);
    }
}
