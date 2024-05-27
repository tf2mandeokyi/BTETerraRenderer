package com.mndk.bteterrarenderer.draco.util.vector;

import com.mndk.bteterrarenderer.draco.core.DracoVector;

public class Draco2d1dVector<E> extends Draco2dVector<DracoVector<E>> {

    public Draco2d1dVector() {
        super(DracoVector::new);
    }

    public E get(int listListIndex, int listIndex, int index) {
        return this.computeIfAbsent(listListIndex).computeIfAbsent(listIndex).get(index);
    }

    public void set(int listListIndex, int listIndex, int index, E element) {
        this.computeIfAbsent(listListIndex).computeIfAbsent(listIndex).set(index, element);
    }
}
