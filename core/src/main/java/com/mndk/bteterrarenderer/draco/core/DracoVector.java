package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.draco.util.vector.DracoVectorView;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class DracoVector<E> extends ArrayList<E> {
    @Nonnull
    private final Supplier<E> uninitializedGetter;

    public DracoVector() {
        this(() -> null);
    }

    public DracoVector(E uninitialized) {
        this(() -> uninitialized);
    }

    @Override
    public E get(int index) {
        if(index < 0 || index >= size()) {
            return uninitializedGetter.get();
        }
        return super.get(index);
    }

    @Override
    public E set(int index, E element) {
        this.updateSize(index);
        return super.set(index, element);
    }

    public void computeSet(int index, Function<E, E> function) {
        this.updateSize(index);
        super.set(index, function.apply(this.get(index)));
    }

    public E computeIfAbsent(int index) {
        this.updateSize(index);
        return super.get(index);
    }

    // These methods below are to make my life easier because 1:1 matching is easy

    public E back() {
        return this.get(this.size() - 1);
    }

    public void push_back(E element) {
        this.add(element);
    }

    public E pop_back() {
        return this.remove(this.size() - 1);
    }

    public DracoVector<E> assign(int count, E element) {
        this.clear();
        for(int i = 0; i < count; i++) {
            this.add(element);
        }
        return this;
    }

    public boolean empty() {
        return this.isEmpty();
    }

    public DracoVector<E> copy() {
        DracoVector<E> result = new DracoVector<>();
        result.addAll(this);
        return result;
    }

    public <T> DracoVector<T> map(Function<E, T> function) {
        DracoVector<T> result = new DracoVector<>();
        for(int i = 0; i < this.size(); i++) {
            result.add(function.apply(this.get(i)));
        }
        return result;
    }

    public void resize(int size, E defaultElement) {
        // Should work the same as c++'s vector::resize
        if(size > this.size()) {
            for(int i = this.size(); i < size; i++) {
                this.push_back(defaultElement);
            }
        }
        else if(size < this.size()) {
            for(int i = this.size(); i > size; i--) {
                this.pop_back();
            }
        }
    }

    public DracoVectorView<E> view(int index) {
        return new DracoVectorView<>(this, index);
    }

    // Draco 2.3: General Conventions
    // - "Unless otherwise noted, array element assignment will increase the size of the array to include the element. (...)"
    private void updateSize(int accessIndex) {
        for(int i = this.size(); i <= accessIndex; i++) {
            this.push_back(this.uninitializedGetter.get());
        }
    }
}
