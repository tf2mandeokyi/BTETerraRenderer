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
import com.mndk.bteterrarenderer.datatype.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;

import javax.annotation.Nonnull;

public abstract class VectorD<S, V extends VectorD<S, V>> implements Comparable<V> {

    public static D2<Integer> int2() { return new D2<>(DataType.int32()); }
    public static D3<Integer> int3() { return new D3<>(DataType.int32()); }
    public static D2<Long> long2() { return new D2<>(DataType.int64()); }
    public static D3<Long> long3() { return new D3<>(DataType.int64()); }
    public static D2<Float> float2() { return new D2<>(DataType.float32()); }
    public static D3<Float> float3() { return new D3<>(DataType.float32()); }
    public static D2<Integer> int2(int x, int y) { return new D2<>(DataType.int32(), x, y); }
    public static D3<Integer> int3(int x, int y, int z) { return new D3<>(DataType.int32(), x, y, z); }
    public static D2<Long> long2(long x, long y) { return new D2<>(DataType.int64(), x, y); }
    public static D3<Long> long3(long x, long y, long z) { return new D3<>(DataType.int64(), x, y, z); }
    public static D2<ULong> uLong2(ULong x, ULong y) { return new D2<>(DataType.uint64(), x, y); }
    public static D3<ULong> uLong3(ULong x, ULong y, ULong z) { return new D3<>(DataType.uint64(), x, y, z); }
    public static D2<Float> float2(float x, float y) { return new D2<>(DataType.float32(), x, y); }
    public static D3<Float> float3(float x, float y, float z) { return new D3<>(DataType.float32(), x, y, z); }
    public static <T> D2<Integer> int2(D2<T> srcVector) { return new D2<>(DataType.int32(), srcVector); }
    public static <T> D3<Integer> int3(D3<T> srcVector) { return new D3<>(DataType.int32(), srcVector); }
    public static <T> D2<Long> long2(D2<T> srcVector) { return new D2<>(DataType.int64(), srcVector); }
    public static <T> D3<Long> long3(D3<T> srcVector) { return new D3<>(DataType.int64(), srcVector); }
    public static <T> D2<ULong> uLong2(D2<T> srcVector) { return new D2<>(DataType.uint64(), srcVector); }
    public static <T> D3<ULong> uLong3(D3<T> srcVector) { return new D3<>(DataType.uint64(), srcVector); }
    public static <T> D2<Float> float2(D2<T> srcVector) { return new D2<>(DataType.float32(), srcVector); }
    public static <T> D3<Float> float3(D3<T> srcVector) { return new D3<>(DataType.float32(), srcVector); }

    private final Pointer<S> v;

    private VectorD(DataNumberType<S> type) {
        int dimension = this.getDimension();
        this.v = type.newArray(dimension);
        for (int i = 0; i < dimension; ++i) {
            v.set(i, type.from(0));
        }
    }

    /**
     * Constructs the vector from another vector with a different data type or a
     * different number of components. If the {@code src_vector} has more components
     * than {@code this} vector, the excess components are truncated. If the
     * {@code src_vector} has fewer components than {@code this} vector, the remaining
     * components are padded with 0.<br>
     * Note that the constructor is intentionally explicit to avoid accidental
     * conversions between different vector types.
     */
    private <T> VectorD(DataNumberType<S> type, VectorD<T, ? extends VectorD<T, ?>> srcVector) {
        this(type);
        DataNumberType<T> srcType = srcVector.getElementType();
        for (int i = 0; i < this.getDimension(); ++i) {
            if (i < srcVector.getDimension()) {
                v.set(i, type.from(srcType, srcVector.v.get(i)));
            } else {
                v.set(i, type.from(0));
            }
        }
    }

    public final DataNumberType<S> getElementType() {
        return v.getType().asNumber();
    }

    protected abstract V create();
    protected abstract int getDimension();

    public Pointer<S> getPointer() { return v; }
    public Pointer<S> getPointer(int index) { return v.add(index); }

    public S get(int i) { return v.get(i); }
    public void set(int i, S value) { v.set(i, value); }

    public V negate() {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.negate(v.get(i)));
        return ret;
    }

    public V add(VectorD<S, V> o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.add(v.get(i), o.v.get(i)));
        return ret;
    }

    public V subtract(VectorD<S, V> o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.sub(v.get(i), o.v.get(i)));
        return ret;
    }

    public V multiply(VectorD<S, V> o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.mul(v.get(i), o.v.get(i)));
        return ret;
    }

    public V multiply(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.mul(v.get(i), o));
        return ret;
    }

    public V divide(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.div(v.get(i), o));
        return ret;
    }

    public V add(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.add(v.get(i), o));
        return ret;
    }

    public V subtract(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.sub(v.get(i), o));
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorD)) return false;
        VectorD<?, ?> o = (VectorD<?, ?>) obj;
        int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) {
            if (!this.get(i).equals(o.get(i))) return false;
        }
        return true;
    }

    @Override
    public int compareTo(@Nonnull V o) {
        DataNumberType<S> type = this.getElementType();
        int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) {
            if (type.lt(this.get(i), o.get(i))) return -1;
            if (type.gt(this.get(i), o.get(i))) return 1;
        }
        return 0;
    }

    public S squaredNorm() {
        return this.dot(this);
    }

    public S absSum() {
        DataNumberType<S> type = this.getElementType();
        S result = type.from(0);
        S max = type.max();
        int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) {
            S nextValue = type.abs(v.get(i));
            if (type.gt(result, type.sub(max, nextValue))) {
                return max;
            }
            result = type.add(result, nextValue);
        }
        return result;
    }

    public S dot(VectorD<S, V> o) {
        DataNumberType<S> type = this.getElementType();
        S ret = type.from(0); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret = type.add(ret, type.mul(v.get(i), o.v.get(i)));
        return ret;
    }

    public void normalize() {
        DataNumberType<S> type = this.getElementType();
        S magnitude = type.sqrt(this.squaredNorm());
        if (type.equals(magnitude, 0)) return;
        int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) v.set(i, type.div(v.get(i), magnitude));
    }

    public V getNormalized() {
        V ret = this.create(); int dimension = this.getDimension();
        for (int i = 0; i < dimension; ++i) ret.set(i, v.get(i));
        ret.normalize();
        return ret;
    }

    public S maxCoeff() {
        DataNumberType<S> type = this.getElementType();
        S max = v.get(0); int dimension = this.getDimension();
        for (int i = 1; i < dimension; ++i) {
            S next = v.get(i);
            if (type.gt(next, max)) max = next;
        }
        return max;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int dimension = this.getDimension();
        for (int i = 0; i < dimension - 1; ++i) sb.append(v.get(i)).append(" ");
        sb.append(v.get(dimension - 1));
        return sb.toString();
    }

    public S minCoeff() {
        DataNumberType<S> type = this.getElementType();
        S min = v.get(0); int dimension = this.getDimension();
        for (int i = 1; i < dimension; ++i) {
            S next = v.get(i);
            if (type.lt(next, min)) min = next;
        }
        return min;
    }

    public static <S, V extends VectorD<S, V>> S squaredDistance(V v1, V v2) {
        if (v1.getDimension() != v2.getDimension()) throw new IllegalArgumentException("Vectors must have the same dimension.");
        DataNumberType<S> type = v1.getElementType();
        S difference;
        S squaredDistance = type.from(0);
        for (int i = 0; i < v1.getDimension(); ++i) {
            if (type.ge(v1.get(i), v2.get(i))) {
                difference = type.sub(v1.get(i), v2.get(i));
            } else {
                difference = type.sub(v2.get(i), v1.get(i));
            }
            squaredDistance = type.add(squaredDistance, type.mul(difference, difference));
        }
        return squaredDistance;
    }

    public static <S> D3<S> crossProduct(D3<S> u, D3<S> v) {
        if (u.getDimension() != 3 || v.getDimension() != 3) {
            throw new IllegalArgumentException("Cross product is only defined for 3D vectors.");
        }
        DataNumberType<S> type = u.getElementType();
        if (type.isUnsigned()) {
            throw new IllegalArgumentException("Cross product is only defined for signed data types.");
        }
        D3<S> r = u.create();
        r.set(0, type.sub(type.mul(u.get(1), v.get(2)), type.mul(u.get(2), v.get(1))));
        r.set(1, type.sub(type.mul(u.get(2), v.get(0)), type.mul(u.get(0), v.get(2))));
        r.set(2, type.sub(type.mul(u.get(0), v.get(1)), type.mul(u.get(1), v.get(0))));
        return r;
    }

    /** {@code Vector2} */
    public static final class D2<S> extends VectorD<S, D2<S>> {
        public D2(DataNumberType<S> type, S s0, S s1) {
            this(type);
            this.set(0, s0);
            this.set(1, s1);
        }
        public D2(DataNumberType<S> type) { super(type); }
        public <T> D2(DataNumberType<S> type, D2<T> srcVector) { super(type, srcVector); }
        @Override protected int getDimension() { return 2; }
        @Override protected D2<S> create() { return new D2<>(this.getElementType()); }
    }
    /** {@code Vector3} */
    public static final class D3<S> extends VectorD<S, D3<S>> {
        public D3(DataNumberType<S> type, S s0, S s1, S s2) {
            this(type);
            this.set(0, s0);
            this.set(1, s1);
            this.set(2, s2);
        }
        public D3(DataNumberType<S> type) { super(type); }
        public <T> D3(DataNumberType<S> type, D3<T> srcVector) { super(type, srcVector); }
        @Override protected int getDimension() { return 3; }
        @Override protected D3<S> create() { return new D3<>(this.getElementType()); }
    }
    /** {@code Vector4} */
    public static final class D4<S> extends VectorD<S, D4<S>> {
        public D4(DataNumberType<S> type, S s0, S s1, S s2, S s3) {
            this(type);
            this.set(0, s0);
            this.set(1, s1);
            this.set(2, s2);
            this.set(3, s3);
        }
        public D4(DataNumberType<S> type) { super(type); }
        public <T> D4(DataNumberType<S> type, D4<T> srcVector) { super(type, srcVector); }
        @Override protected int getDimension() { return 4; }
        @Override protected D4<S> create() { return new D4<>(this.getElementType()); }
    }
    /** {@code Vector5} */
    public static final class D5<S> extends VectorD<S, D5<S>> {
        public D5(DataNumberType<S> type, S s0, S s1, S s2, S s3, S s4) {
            this(type);
            this.set(0, s0);
            this.set(1, s1);
            this.set(2, s2);
            this.set(3, s3);
            this.set(4, s4);
        }
        public D5(DataNumberType<S> type) { super(type); }
        public <T> D5(DataNumberType<S> type, D5<T> srcVector) { super(type, srcVector); }
        @Override protected int getDimension() { return 5; }
        @Override protected D5<S> create() { return new D5<>(this.getElementType()); }
    }
    /** {@code Vector6} */
    public static final class D6<S> extends VectorD<S, D6<S>> {
        public D6(DataNumberType<S> type, S s0, S s1, S s2, S s3, S s4, S s5) {
            this(type);
            this.set(0, s0);
            this.set(1, s1);
            this.set(2, s2);
            this.set(3, s3);
            this.set(4, s4);
            this.set(5, s5);
        }
        public D6(DataNumberType<S> type) { super(type); }
        public <T> D6(DataNumberType<S> type, D6<T> srcVector) { super(type, srcVector); }
        @Override protected int getDimension() { return 6; }
        @Override protected D6<S> create() { return new D6<>(this.getElementType()); }
    }
    /** {@code Vector7} */
    public static final class D7<S> extends VectorD<S, D7<S>> {
        public D7(DataNumberType<S> type, S s0, S s1, S s2, S s3, S s4, S s5, S s6) {
            this(type);
            this.set(0, s0);
            this.set(1, s1);
            this.set(2, s2);
            this.set(3, s3);
            this.set(4, s4);
            this.set(5, s5);
            this.set(6, s6);
        }
        public D7(DataNumberType<S> type) { super(type); }
        public <T> D7(DataNumberType<S> type, D7<T> srcVector) { super(type, srcVector); }
        @Override protected int getDimension() { return 7; }
        @Override protected D7<S> create() { return new D7<>(this.getElementType()); }
    }
}
