package com.mndk.bteterrarenderer.draco.core;

import lombok.Getter;

import javax.annotation.Nonnull;

public abstract class VectorD<S, V extends VectorD<S, V>> implements Comparable<V> {

    private final S[] v;
    @Getter
    private final int dimension;

    private VectorD(int dimension) {
        this.dimension = dimension;
        DataType<S> dataType = this.getDataType();
        this.v = dataType.newArray(dimension);
        for (int i = 0; i < dimension; ++i) {
            this.v[i] = dataType.staticCast(0);
        }
    }

    protected abstract DataType<S> getDataType();
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
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) {
            if (i < srcVector.dimension) {
                this.v[i] = dataType.staticCast(srcVector.v[i]);
            } else {
                this.v[i] = dataType.staticCast(0);
            }
        }
    }

    protected <T> void init(T s0, T s1) {
        DataType<S> dataType = this.getDataType();
        this.set(0, dataType.staticCast(s0));
        this.set(1, dataType.staticCast(s1));
    }
    protected <T> void init(T s0, T s1, T s2) {
        DataType<S> dataType = this.getDataType();
        this.set(0, dataType.staticCast(s0));
        this.set(1, dataType.staticCast(s1));
        this.set(2, dataType.staticCast(s2));
    }
    protected <T> void init(T s0, T s1, T s2, T s3) {
        DataType<S> dataType = this.getDataType();
        this.set(0, dataType.staticCast(s0));
        this.set(1, dataType.staticCast(s1));
        this.set(2, dataType.staticCast(s2));
        this.set(3, dataType.staticCast(s3));
    }
    protected <T> void init(T s0, T s1, T s2, T s3, T s4) {
        this.init(s0, s1, s2, s3);
        DataType<S> dataType = this.getDataType();
        this.set(4, dataType.staticCast(s4));
    }
    protected <T> void init(T s0, T s1, T s2, T s3, T s4, T s5) {
        this.init(s0, s1, s2, s3);
        DataType<S> dataType = this.getDataType();
        this.set(4, dataType.staticCast(s4));
        this.set(5, dataType.staticCast(s5));
    }
    protected <T> void init(T s0, T s1, T s2, T s3, T s4, T s5, T s6) {
        this.init(s0, s1, s2, s3, s4, s5);
        DataType<S> dataType = this.getDataType();
        this.set(6, dataType.staticCast(s6));
    }

    public S get(int i) {
        return v[i];
    }
    public void set(int i, S value) {
        v[i] = value;
    }

    public V negate() {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.negate(v[i]));
        return ret;
    }

    public V add(VectorD<S, V> o) {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.add(v[i], o.v[i]));
        return ret;
    }

    public V subtract(VectorD<S, V> o) {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.subtract(v[i], o.v[i]));
        return ret;
    }

    public V multiply(VectorD<S, V> o) {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.multiply(v[i], o.v[i]));
        return ret;
    }

    public V multiply(S o) {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.multiply(v[i], o));
        return ret;
    }

    public V divide(S o) {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.divide(v[i], o));
        return ret;
    }

    public V add(S o) {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.add(v[i], o));
        return ret;
    }

    public V subtract(S o) {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) ret.set(i, dataType.subtract(v[i], o));
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VectorD)) return false;
        VectorD<?, ?> o = (VectorD<?, ?>) obj;
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) if (!v[i].equals(o.v[i])) return false;
        return true;
    }

    @Override
    public int compareTo(@Nonnull V o) {
        DataType<S> dataType = this.getDataType();
        for (int i = 0; i < dimension; ++i) {
            if (dataType.lt(v[i], o.get(i))) return -1;
            if (dataType.gt(v[i], o.get(i))) return 1;
        }
        return 0;
    }

    public S squaredNorm() {
        return this.dot(this);
    }

    public S absSum() {
        DataType<S> dataType = this.getDataType();
        S result = dataType.staticCast(0);
        for (int i = 0; i < dimension; ++i) {
            S nextValue = dataType.abs(v[i]);
            if (dataType.gt(result, dataType.subtract(dataType.max(), nextValue))) {
                return dataType.max();
            }
            result = dataType.add(result, nextValue);
        }
        return result;
    }

    public S dot(VectorD<S, V> o) {
        DataType<S> dataType = this.getDataType();
        S ret = dataType.staticCast(0);
        for (int i = 0; i < dimension; ++i) ret = dataType.add(ret, dataType.multiply(v[i], o.v[i]));
        return ret;
    }

    public void normalize() {
        DataType<S> dataType = this.getDataType();
        S magnitude = dataType.sqrt(this.squaredNorm());
        if (dataType.equals(magnitude, dataType.staticCast(0))) return;
        for (int i = 0; i < dimension; ++i) v[i] = dataType.divide(v[i], magnitude);
    }

    public V getNormalized() {
        V ret = this.create();
        DataType<S> dataType = this.getDataType();
        for(int i = 0; i < dimension; ++i) ret.set(i, v[i]);
        ret.normalize();
        return ret;
    }

    public S maxCoeff() {
        DataType<S> dataType = this.getDataType();
        S max = v[0];
        for (int i = 1; i < dimension; ++i) if (dataType.gt(v[i], max)) max = v[i];
        return max;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dimension - 1; ++i) sb.append(v[i]).append(" ");
        sb.append(v[dimension - 1]);
        return sb.toString();
    }

    public S minCoeff() {
        DataType<S> dataType = this.getDataType();
        S min = v[0];
        for (int i = 1; i < dimension; ++i) if (dataType.lt(v[i], min)) min = v[i];
        return min;
    }

    public static <S, V extends VectorD<S, V>> S squaredDistance(V v1, V v2) {
        if(v1.getDimension() != v2.getDimension()) throw new IllegalArgumentException("Vectors must have the same dimension.");
        DataType<S> dataType = v1.getDataType();
        S difference;
        S squaredDistance = dataType.staticCast(0);
        for (int i = 0; i < v1.getDimension(); ++i) {
            if (dataType.ge(v1.get(i), v2.get(i))) {
                difference = dataType.subtract(v1.get(i), v2.get(i));
            } else {
                difference = dataType.subtract(v2.get(i), v1.get(i));
            }
            squaredDistance = dataType.add(squaredDistance, dataType.multiply(difference, difference));
        }
        return squaredDistance;
    }

    public static <S, V extends VectorD<S, V>> V crossProduct(V u, V v) {
        if(u.getDimension() != 3 || v.getDimension() != 3) throw new IllegalArgumentException("Cross product is only defined for 3D vectors.");
        DataType<S> dataType = u.getDataType();
        if(dataType.isUnsigned()) throw new IllegalArgumentException("Cross product is only defined for signed data types.");
        V r = u.create();
        r.set(0, dataType.subtract(dataType.multiply(u.get(1), v.get(2)), dataType.multiply(u.get(2), v.get(1))));
        r.set(1, dataType.subtract(dataType.multiply(u.get(2), v.get(0)), dataType.multiply(u.get(0), v.get(2))));
        r.set(2, dataType.subtract(dataType.multiply(u.get(0), v.get(1)), dataType.multiply(u.get(1), v.get(0))));
        return r;
    }

    /** {@code Vector2f} */
    public static class F2 extends VectorD<Float, F2> {
        public F2(float s0, float s1) {
            super(2);
            this.init(s0, s1);
        }
        protected DataType<Float> getDataType() { return DataType.FLOAT32; }
        protected F2 create() { return new F2(0, 0); }
    }
    /** {@code Vector3f} */
    public static class F3 extends VectorD<Float, F3> {
        public F3(float s0, float s1, float s2) {
            super(3);
            this.init(s0, s1, s2);
        }
        protected DataType<Float> getDataType() { return DataType.FLOAT32; }
        protected F3 create() { return new F3(0, 0, 0); }
    }
    /** {@code Vector4f} */
    public static class F4 extends VectorD<Float, F4> {
        public F4(float s0, float s1, float s2, float s3) {
            super(4);
            this.init(s0, s1, s2, s3);
        }
        protected DataType<Float> getDataType() { return DataType.FLOAT32; }
        protected F4 create() { return new F4(0, 0, 0, 0); }
    }
    /** {@code Vector5f} */
    public static class F5 extends VectorD<Float, F5> {
        public F5(float s0, float s1, float s2, float s3, float s4) {
            super(5);
            this.init(s0, s1, s2, s3, s4);
        }
        protected DataType<Float> getDataType() { return DataType.FLOAT32; }
        protected F5 create() { return new F5(0, 0, 0, 0, 0); }
    }
    /** {@code Vector6f} */
    public static class F6 extends VectorD<Float, F6> {
        public F6(float s0, float s1, float s2, float s3, float s4, float s5) {
            super(6);
            this.init(s0, s1, s2, s3, s4, s5);
        }
        protected DataType<Float> getDataType() { return DataType.FLOAT32; }
        protected F6 create() { return new F6(0, 0, 0, 0, 0, 0); }
    }
    /** {@code Vector7f} */
    public static class F7 extends VectorD<Float, F7> {
        public F7(float s0, float s1, float s2, float s3, float s4, float s5, float s6) {
            super(7);
            this.init(s0, s1, s2, s3, s4, s5, s6);
        }
        protected DataType<Float> getDataType() { return DataType.FLOAT32; }
        protected F7 create() { return new F7(0, 0, 0, 0, 0, 0, 0); }
    }

    /** {@code Vector2ui} */
    public static class UI2 extends VectorD<Long, UI2> {
        public UI2(int s0, int s1) {
            super(2);
            this.init(s0, s1);
        }
        protected DataType<Long> getDataType() { return DataType.UINT32; }
        protected UI2 create() { return new UI2(0, 0); }
    }
    /** {@code Vector3ui} */
    public static class UI3 extends VectorD<Long, UI3> {
        public UI3(int s0, int s1, int s2) {
            super(3);
            this.init(s0, s1, s2);
        }
        protected DataType<Long> getDataType() { return DataType.UINT32; }
        protected UI3 create() { return new UI3(0, 0, 0); }
    }
    /** {@code Vector4ui} */
    public static class UI4 extends VectorD<Long, UI4> {
        public UI4(int s0, int s1, int s2, int s3) {
            super(4);
            this.init(s0, s1, s2, s3);
        }
        protected DataType<Long> getDataType() { return DataType.UINT32; }
        protected UI4 create() { return new UI4(0, 0, 0, 0); }
    }
    /** {@code Vector5ui} */
    public static class UI5 extends VectorD<Long, UI5> {
        public UI5(int s0, int s1, int s2, int s3, int s4) {
            super(5);
            this.init(s0, s1, s2, s3, s4);
        }
        protected DataType<Long> getDataType() { return DataType.UINT32; }
        protected UI5 create() { return new UI5(0, 0, 0, 0, 0); }
    }
    /** {@code Vector6ui} */
    public static class UI6 extends VectorD<Long, UI6> {
        public UI6(int s0, int s1, int s2, int s3, int s4, int s5) {
            super(6);
            this.init(s0, s1, s2, s3, s4, s5);
        }
        protected DataType<Long> getDataType() { return DataType.UINT32; }
        protected UI6 create() { return new UI6(0, 0, 0, 0, 0, 0); }
    }
    /** {@code Vector7ui} */
    public static class UI7 extends VectorD<Long, UI7> {
        public UI7(int s0, int s1, int s2, int s3, int s4, int s5, int s6) {
            super(7);
            this.init(s0, s1, s2, s3, s4, s5, s6);
        }
        protected DataType<Long> getDataType() { return DataType.UINT32; }
        protected UI7 create() { return new UI7(0, 0, 0, 0, 0, 0, 0); }
    }
}
