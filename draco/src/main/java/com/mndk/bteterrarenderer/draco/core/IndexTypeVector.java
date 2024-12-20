/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.vector.CppVector;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class IndexTypeVector<I extends IndexType<I>, E> implements Iterable<E> {

    private final CppVector<E> vector;

    public IndexTypeVector(DataType<E> type) {
        this.vector = new CppVector<>(type);
    }
    public IndexTypeVector(DataType<E> type, long size) {
        this.vector = new CppVector<>(type, size);
    }
    public IndexTypeVector(DataType<E> type, long size, E value) {
        this.vector = new CppVector<>(type, size, value);
    }

    public IndexTypeVector(Supplier<E> defaultValueMaker) {
        this.vector = new CppVector<>(DataType.object(defaultValueMaker));
    }
    public IndexTypeVector(Supplier<E> defaultValueMaker, long size) {
        this.vector = new CppVector<>(DataType.object(defaultValueMaker), size);
    }

    public void clear() { vector.clear(); }
    public void reserve(long size) { vector.reserve(size); }
    public void resize(long size) { vector.resize(size); }
    public void resize(long size, E value) { vector.resize(size, value); }
    public void assign(long size, E value) { vector.assign(size, value); }
    public void erase(I index) { vector.erase(index.getValue()); }

    public void swap(IndexTypeVector<I, E> other) { vector.swap(other.vector); }

    public long size() { return vector.size(); }
    public boolean isEmpty() { return vector.isEmpty(); }

    public void pushBack(E value) { vector.pushBack(value); }

    public E get(I index) { return vector.get(index.getValue()); }
    public void set(I index, E value) { vector.set(index.getValue(), value); }
    public void set(I index, Function<E, E> setter) { vector.set(index.getValue(), setter); }

    @Override @Nonnull public Iterator<E> iterator() { return vector.iterator(); }
    @Override public String toString() { return vector.toString(); }
    @Override public int hashCode() { return vector.hashCode(); }
    @Override public boolean equals(Object obj) {
        return obj instanceof IndexTypeVector && vector.equals(((IndexTypeVector<?, ?>) obj).vector);
    }
}
