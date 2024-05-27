package com.mndk.bteterrarenderer.draco.util.vector;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.draco.core.DracoVector;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.*;

@RequiredArgsConstructor
public class DracoVectorView<E> extends DracoVector<E> {

    private final DracoVector<E> vector;
    private final int offset;

    @Override
    public int size() {
        return vector.size() - offset;
    }

    @Override
    public boolean isEmpty() {
        return vector.size() - offset <= 0;
    }

    @Override
    public boolean contains(Object o) {
        for(int i = 0; i < this.size(); i++) {
            if(this.get(i).equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public Iterator<E> iterator() {
        return new ViewIterator();
    }

    @Nonnull
    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        for(int i = 0; i < size(); i++) {
            array[i] = this.get(i);
        }
        return array;
    }

    @Nonnull
    @Override
    public <T> T[] toArray(T[] a) {
        return BTRUtil.uncheckedCast(Arrays.copyOf(this.toArray(), this.size(), a.getClass()));
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("vector views cannot add elements");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("vector views cannot remove elements");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends E> c) {
        throw new UnsupportedOperationException("vector views cannot add elements");
    }

    @Override
    public boolean addAll(int index, @Nonnull Collection<? extends E> c) {
        throw new UnsupportedOperationException("vector views cannot add elements");
    }

    @Override
    public boolean removeAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("vector views cannot remove elements");
    }

    @Override
    public boolean retainAll(@Nonnull Collection<?> c) {
        throw new UnsupportedOperationException("vector views cannot remove elements");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("vector views cannot remove elements");
    }

    @Override
    public E get(int index) {
        return vector.get(index + offset);
    }

    @Override
    public E set(int index, E element) {
        return vector.set(index + offset, element);
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException("vector views cannot add elements");
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException("vector views cannot remove elements");
    }

    @Override
    public int indexOf(Object o) {
        int result = vector.indexOf(o) - offset;
        return result < 0 ? -1 : result;
    }

    @Override
    public int lastIndexOf(Object o) {
        int result = vector.lastIndexOf(o) - offset;
        return result < 0 ? -1 : result;
    }

    @Nonnull
    @Override
    public ListIterator<E> listIterator() {
        return new ViewListIterator();
    }

    @Nonnull
    @Override
    public ListIterator<E> listIterator(int index) {
        return new ViewListIterator(index);
    }

    @Nonnull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return vector.subList(fromIndex + offset, toIndex + offset);
    }

    private class ViewIterator implements Iterator<E> {
        int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public E next() {
            return get(cursor++);
        }
    }

    private class ViewListIterator extends ViewIterator implements ListIterator<E> {
        ViewListIterator(int cursor) {
            this.cursor = cursor;
        }
        ViewListIterator() {}

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public E previous() {
            return get(cursor--);
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("vector views cannot remove elements");
        }

        @Override
        public void set(E e) {
            DracoVectorView.this.set(cursor, e);
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException("vector views cannot add elements");
        }
    }
}
