package com.mndk.bteterrarenderer.draco.util.vector;

import com.mndk.bteterrarenderer.draco.core.DracoVector;
import lombok.NonNull;

import java.util.function.Supplier;

public class Draco2dVector<E> extends DracoVector<DracoVector<E>> {

    public Draco2dVector() {
        super(DracoVector::new);
    }

    public Draco2dVector(@NonNull Supplier<E> uninitializedGetter) {
        super(() -> new DracoVector<>(uninitializedGetter));
    }

    public int size(int listIndex) {
        return this.computeIfAbsent(listIndex).size();
    }

    public void push_back(int listIndex, E element) {
        this.computeIfAbsent(listIndex).add(element);
    }

    public E pop_back(int listIndex) {
        return this.computeIfAbsent(listIndex).pop_back();
    }

    public void set(int listIndex, int index, E element) {
        this.computeIfAbsent(listIndex).set(index, element);
    }

    public E get(int listIndex, int index) {
        return this.computeIfAbsent(listIndex).get(index);
    }

    public boolean empty(int listIndex) {
        return this.computeIfAbsent(listIndex).empty();
    }

    public void assign(int listIndex, int size, E element) {
        this.computeIfAbsent(listIndex).assign(size, element);
    }

    public void resize(int listIndex, int size, E element) {
        this.computeIfAbsent(listIndex).resize(size, element);
    }

    @Override
    public DracoVector<DracoVector<E>> copy() {
        return this.copy2d();
    }

    public Draco2dVector<E> copy2d() {
        Draco2dVector<E> result = new Draco2dVector<>();
        for(int listIndex = 0; listIndex < size(); listIndex++) {
            for(int index = 0; index < size(listIndex); index++) {
                result.set(listIndex, index, this.get(listIndex, index));
            }
        }
        return result;
    }
}
