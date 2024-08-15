package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.DataNumberType;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import lombok.Getter;

import javax.annotation.Nonnull;

public abstract class VectorD<S, V extends VectorD<S, V>> implements Comparable<V> {

    private final Pointer<S> v;
    @Getter private final int dimension;

    protected VectorD(int dimension) {
        this.dimension = dimension;
        DataNumberType<S> type = this.getElementType();
        this.v = type.newArray(dimension);
        for (int i = 0; i < dimension; ++i) {
            v.set(i, type.from(0));
        }
    }

    public abstract DataNumberType<S> getElementType();
    protected abstract V create();

    /**
     * Constructs the vector from another vector with a different data type or a
     * different number of components. If the {@code src_vector} has more components
     * than {@code this} vector, the excess components are truncated. If the
     * {@code src_vector} has fewer components than {@code this} vector, the remaining
     * components are padded with 0.<br>
     * Note that the constructor is intentionally explicit to avoid accidental
     * conversions between different vector types.
     */
    public <T> VectorD(VectorD<T, ? extends VectorD<T, ?>> srcVector) {
        this(srcVector.dimension);
        DataNumberType<S> type = this.getElementType();
        DataNumberType<T> srcType = srcVector.getElementType();
        for (int i = 0; i < dimension; ++i) {
            if (i < srcVector.dimension) {
                v.set(i, type.from(srcType, srcVector.v.get(i)));
            } else {
                v.set(i, type.from(0));
            }
        }
    }

    public Pointer<S> getPointer() { return v; }
    public Pointer<S> getPointer(int index) { return v.add(index); }

    protected void init(S s0, S s1) {
        this.set(0, s0);
        this.set(1, s1);
    }
    protected void init(S s0, S s1, S s2) {
        this.set(0, s0);
        this.set(1, s1);
        this.set(2, s2);
    }
    protected void init(S s0, S s1, S s2, S s3) {
        this.set(0, s0);
        this.set(1, s1);
        this.set(2, s2);
        this.set(3, s3);
    }
    protected void init(S s0, S s1, S s2, S s3, S s4) {
        this.init(s0, s1, s2, s3);
        this.set(4, s4);
    }
    protected void init(S s0, S s1, S s2, S s3, S s4, S s5) {
        this.init(s0, s1, s2, s3);
        this.set(4, s4);
        this.set(5, s5);
    }
    protected void init(S s0, S s1, S s2, S s3, S s4, S s5, S s6) {
        this.init(s0, s1, s2, s3, s4, s5);
        this.set(6, s6);
    }

    public S get(int i) { return v.get(i); }
    public void set(int i, S value) { v.set(i, value); }

    public V negate() {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.negate(v.get(i)));
        return ret;
    }

    public V add(VectorD<S, V> o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.add(v.get(i), o.v.get(i)));
        return ret;
    }

    public V subtract(VectorD<S, V> o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.sub(v.get(i), o.v.get(i)));
        return ret;
    }

    public V multiply(VectorD<S, V> o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.mul(v.get(i), o.v.get(i)));
        return ret;
    }

    public V multiply(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.mul(v.get(i), o));
        return ret;
    }

    public V divide(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.div(v.get(i), o));
        return ret;
    }

    public V add(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.add(v.get(i), o));
        return ret;
    }

    public V subtract(S o) {
        DataNumberType<S> type = this.getElementType();
        V ret = this.create();
        for (int i = 0; i < dimension; ++i) ret.set(i, type.sub(v.get(i), o));
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorD)) return false;
        VectorD<?, ?> o = (VectorD<?, ?>) obj;
        for (int i = 0; i < dimension; ++i) {
            if (!this.get(i).equals(o.get(i))) return false;
        }
        return true;
    }

    @Override
    public int compareTo(@Nonnull V o) {
        DataNumberType<S> type = this.getElementType();
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
        S ret = type.from(0);
        for (int i = 0; i < dimension; ++i) ret = type.add(ret, type.mul(v.get(i), o.v.get(i)));
        return ret;
    }

    public void normalize() {
        DataNumberType<S> type = this.getElementType();
        S magnitude = type.sqrt(this.squaredNorm());
        if (type.equals(magnitude, 0)) return;
        for (int i = 0; i < dimension; ++i) v.set(i, type.div(v.get(i), magnitude));
    }

    public V getNormalized() {
        V ret = this.create();
        for(int i = 0; i < dimension; ++i) ret.set(i, v.get(i));
        ret.normalize();
        return ret;
    }

    public S maxCoeff() {
        DataNumberType<S> type = this.getElementType();
        S max = v.get(0);
        for (int i = 1; i < dimension; ++i) {
            S next = v.get(i);
            if(type.gt(next, max)) max = next;
        }
        return max;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimension - 1; ++i) sb.append(v.get(i)).append(" ");
        sb.append(v.get(dimension - 1));
        return sb.toString();
    }

    public S minCoeff() {
        DataNumberType<S> type = this.getElementType();
        S min = v.get(0);
        for (int i = 1; i < dimension; ++i) {
            S next = v.get(i);
            if (type.lt(next, min)) min = next;
        }
        return min;
    }

    public static <S, V extends VectorD<S, V>> S squaredDistance(V v1, V v2) {
        if(v1.getDimension() != v2.getDimension()) throw new IllegalArgumentException("Vectors must have the same dimension.");
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

    public static <S, V extends VectorD<S, V>> V crossProduct(V u, V v) {
        if(u.getDimension() != 3 || v.getDimension() != 3) {
            throw new IllegalArgumentException("Cross product is only defined for 3D vectors.");
        }
        DataNumberType<S> type = u.getElementType();
        if(type.isUnsigned()) {
            throw new IllegalArgumentException("Cross product is only defined for signed data types.");
        }
        V r = u.create();
        r.set(0, type.sub(type.mul(u.get(1), v.get(2)), type.mul(u.get(2), v.get(1))));
        r.set(1, type.sub(type.mul(u.get(2), v.get(0)), type.mul(u.get(0), v.get(2))));
        r.set(2, type.sub(type.mul(u.get(0), v.get(1)), type.mul(u.get(1), v.get(0))));
        return r;
    }

    /** {@code Vector2f} */
    public static class F2 extends VectorD<Float, F2> {
        public F2() { this(0, 0); }
        public F2(float s0, float s1) {
            super(2); this.init(s0, s1);
        }
        public <T> F2(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Float> getElementType() { return DataType.float32(); }
        protected F2 create() { return new F2(); }
    }
    /** {@code Vector3f} */
    public static class F3 extends VectorD<Float, F3> {
        public F3() { this(0, 0, 0); }
        public F3(float s0, float s1, float s2) {
            super(3); this.init(s0, s1, s2);
        }
        public <T> F3(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Float> getElementType() { return DataType.float32(); }
        protected F3 create() { return new F3(); }
    }
    /** {@code Vector4f} */
    public static class F4 extends VectorD<Float, F4> {
        public F4() { this(0, 0, 0, 0); }
        public F4(float s0, float s1, float s2, float s3) {
            super(4); this.init(s0, s1, s2, s3);
        }
        public <T> F4(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Float> getElementType() { return DataType.float32(); }
        protected F4 create() { return new F4(); }
    }
    /** {@code Vector5f} */
    public static class F5 extends VectorD<Float, F5> {
        public F5() { this(0, 0, 0, 0, 0); }
        public F5(float s0, float s1, float s2, float s3, float s4) {
            super(5); this.init(s0, s1, s2, s3, s4);
        }
        public <T> F5(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Float> getElementType() { return DataType.float32(); }
        protected F5 create() { return new F5(); }
    }
    /** {@code Vector6f} */
    public static class F6 extends VectorD<Float, F6> {
        public F6() { this(0, 0, 0, 0, 0, 0); }
        public F6(float s0, float s1, float s2, float s3, float s4, float s5) {
            super(6); this.init(s0, s1, s2, s3, s4, s5);
        }
        public <T> F6(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Float> getElementType() { return DataType.float32(); }
        protected F6 create() { return new F6(); }
    }
    /** {@code Vector7f} */
    public static class F7 extends VectorD<Float, F7> {
        public F7() { this(0, 0, 0, 0, 0, 0, 0); }
        public F7(float s0, float s1, float s2, float s3, float s4, float s5, float s6) {
            super(7); this.init(s0, s1, s2, s3, s4, s5, s6);
        }
        public <T> F7(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Float> getElementType() { return DataType.float32(); }
        protected F7 create() { return new F7(); }
    }

    public static class I2 extends VectorD<Integer, I2> {
        public I2() { this(0, 0); }
        public I2(int s0, int s1) {
            super(2); this.init(s0, s1);
        }
        public <T> I2(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Integer> getElementType() { return DataType.int32(); }
        protected I2 create() { return new I2(); }
    }
    public static class I3 extends VectorD<Integer, I3> {
        public I3() { this(0, 0, 0); }
        public I3(int s0, int s1, int s2) {
            super(3); this.init(s0, s1, s2);
        }
        public <T> I3(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Integer> getElementType() { return DataType.int32(); }
        protected I3 create() { return new I3(); }
    }

    /** {@code Vector2ui} */
    public static class UI2 extends VectorD<UInt, UI2> {
        public UI2() { this(UInt.ZERO, UInt.ZERO); }
        public UI2(UInt s0, UInt s1) {
            super(2); this.init(s0, s1);
        }
        public <T> UI2(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<UInt> getElementType() { return DataType.uint32(); }
        protected UI2 create() { return new UI2(); }
    }
    /** {@code Vector3ui} */
    public static class UI3 extends VectorD<UInt, UI3> {
        public UI3() { this(UInt.ZERO, UInt.ZERO, UInt.ZERO); }
        public UI3(UInt s0, UInt s1, UInt s2) {
            super(3); this.init(s0, s1, s2);
        }
        public <T> UI3(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<UInt> getElementType() { return DataType.uint32(); }
        protected UI3 create() { return new UI3(); }
    }
    /** {@code Vector4ui} */
    public static class UI4 extends VectorD<UInt, UI4> {
        public UI4() { this(UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO); }
        public UI4(UInt s0, UInt s1, UInt s2, UInt s3) {
            super(4); this.init(s0, s1, s2, s3);
        }
        public <T> UI4(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<UInt> getElementType() { return DataType.uint32(); }
        protected UI4 create() { return new UI4(); }
    }
    /** {@code Vector5ui} */
    public static class UI5 extends VectorD<UInt, UI5> {
        public UI5() { this(UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO); }
        public UI5(UInt s0, UInt s1, UInt s2, UInt s3, UInt s4) {
            super(5); this.init(s0, s1, s2, s3, s4);
        }
        public <T> UI5(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<UInt> getElementType() { return DataType.uint32(); }
        protected UI5 create() { return new UI5(); }
    }
    /** {@code Vector6ui} */
    public static class UI6 extends VectorD<UInt, UI6> {
        public UI6() { this(UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO); }
        public UI6(UInt s0, UInt s1, UInt s2, UInt s3, UInt s4, UInt s5) {
            super(6); this.init(s0, s1, s2, s3, s4, s5);
        }
        public <T> UI6(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<UInt> getElementType() { return DataType.uint32(); }
        protected UI6 create() { return new UI6(); }
    }
    /** {@code Vector7ui} */
    public static class UI7 extends VectorD<UInt, UI7> {
        public UI7() { this(UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO, UInt.ZERO); }
        public UI7(UInt s0, UInt s1, UInt s2, UInt s3, UInt s4, UInt s5, UInt s6) {
            super(7); this.init(s0, s1, s2, s3, s4, s5, s6);
        }
        public <T> UI7(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<UInt> getElementType() { return DataType.uint32(); }
        protected UI7 create() { return new UI7(); }
    }

    public static class L2 extends VectorD<Long, L2> {
        public L2() { this(0, 0); }
        public L2(long s0, long s1) {
            super(2); this.init(s0, s1);
        }
        public <T> L2(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Long> getElementType() { return DataType.int64(); }
        protected L2 create() { return new L2(); }
    }
    public static class L3 extends VectorD<Long, L3> {
        public L3() { this(0, 0, 0); }
        public L3(long s0, long s1, long s2) {
            super(3); this.init(s0, s1, s2);
        }
        public <T> L3(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<Long> getElementType() { return DataType.int64(); }
        protected L3 create() { return new L3(); }
    }

    public static class UL2 extends VectorD<ULong, UL2> {
        public UL2() { this(ULong.ZERO, ULong.ZERO); }
        public UL2(ULong s0, ULong s1) {
            super(2); this.init(s0, s1);
        }
        public <T> UL2(VectorD<T, ? extends VectorD<T, ?>> srcVector) { super(srcVector); }
        public DataNumberType<ULong> getElementType() { return DataType.uint64(); }
        protected UL2 create() { return new UL2(); }
    }
}
