package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.core.util.BTRUtil;

public interface DataCalculator<T> {
    // Number properties
    boolean isIntegral();
    boolean isSigned();
    default boolean isFloatingPoint() { return !isIntegral(); }
    default boolean isUnsigned() { return !this.isSigned(); }
    T lowest();
    T min();
    T max();

    // Type conversions
    DataCalculator<?> getSigned();
    DataCalculator<?> getUnsigned();
    default <U> DataCalculator<U> makeSigned() { return BTRUtil.uncheckedCast(this.getSigned()); }
    default <U> DataCalculator<U> makeUnsigned() { return BTRUtil.uncheckedCast(this.getUnsigned()); }

    // Arithmetic operations
    T add(T left, T right);
    default <U> T add(T left, DataCalculator<U> type, U right) { return this.add(left, this.from(type, right)); }
    default T add(T left, int right) { return this.add(left, this.from(right)); }
    default T add(T left, long right) { return this.add(left, this.from(right)); }
    default T add(T left, float right) { return this.add(left, this.from(right)); }
    default T add(T left, double right) { return this.add(left, this.from(right)); }
    default <U> T add(DataCalculator<U> type, U left, T right) { return this.add(this.from(type, left), right); }
    default T add(int left, T right) { return this.add(this.from(left), right); }
    default T add(long left, T right) { return this.add(this.from(left), right); }
    default T add(float left, T right) { return this.add(this.from(left), right); }
    default T add(double left, T right) { return this.add(this.from(left), right); }
    default <U, V> T add(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.add(this.from(leftType, left), this.from(rightType, right));
    }
    T sub(T left, T right);
    default <U> T sub(T left, DataCalculator<U> type, U right) { return this.sub(left, this.from(type, right)); }
    default T sub(T left, int right) { return this.sub(left, this.from(right)); }
    default T sub(T left, long right) { return this.sub(left, this.from(right)); }
    default T sub(T left, float right) { return this.sub(left, this.from(right)); }
    default T sub(T left, double right) { return this.sub(left, this.from(right)); }
    default <U> T sub(DataCalculator<U> type, U left, T right) { return this.sub(this.from(type, left), right); }
    default T sub(int left, T right) { return this.sub(this.from(left), right); }
    default T sub(long left, T right) { return this.sub(this.from(left), right); }
    default T sub(float left, T right) { return this.sub(this.from(left), right); }
    default T sub(double left, T right) { return this.sub(this.from(left), right); }
    default <U, V> T sub(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.sub(this.from(leftType, left), this.from(rightType, right));
    }
    T mul(T left, T right);
    default <U> T mul(T left, DataCalculator<U> type, U right) { return this.mul(left, this.from(type, right)); }
    default T mul(T left, int right) { return this.mul(left, this.from(right)); }
    default T mul(T left, long right) { return this.mul(left, this.from(right)); }
    default T mul(T left, float right) { return this.mul(left, this.from(right)); }
    default T mul(T left, double right) { return this.mul(left, this.from(right)); }
    default <U> T mul(DataCalculator<U> type, U left, T right) { return this.mul(this.from(type, left), right); }
    default T mul(int left, T right) { return this.mul(this.from(left), right); }
    default T mul(long left, T right) { return this.mul(this.from(left), right); }
    default T mul(float left, T right) { return this.mul(this.from(left), right); }
    default T mul(double left, T right) { return this.mul(this.from(left), right); }
    default <U, V> T mul(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.mul(this.from(leftType, left), this.from(rightType, right));
    }
    T div(T left, T right);
    default <U> T div(T left, DataCalculator<U> type, U right) { return this.div(left, this.from(type, right)); }
    default T div(T left, int right) { return this.div(left, this.from(right)); }
    default T div(T left, long right) { return this.div(left, this.from(right)); }
    default T div(T left, float right) { return this.div(left, this.from(right)); }
    default T div(T left, double right) { return this.div(left, this.from(right)); }
    default <U> T div(DataCalculator<U> type, U left, T right) { return this.div(this.from(type, left), right); }
    default T div(int left, T right) { return this.div(this.from(left), right); }
    default T div(long left, T right) { return this.div(this.from(left), right); }
    default T div(float left, T right) { return this.div(this.from(left), right); }
    default T div(double left, T right) { return this.div(this.from(left), right); }
    default <U, V> T div(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.div(this.from(leftType, left), this.from(rightType, right));
    }
    T mod(T left, T right);
    default <U> T mod(T left, DataCalculator<U> type, U right) { return this.mod(left, this.from(type, right)); }
    default T mod(T left, int right) { return this.mod(left, this.from(right)); }
    default T mod(T left, long right) { return this.mod(left, this.from(right)); }
    default T mod(T left, float right) { return this.mod(left, this.from(right)); }
    default T mod(T left, double right) { return this.mod(left, this.from(right)); }
    default <U> T mod(DataCalculator<U> type, U left, T right) { return this.mod(this.from(type, left), right); }
    default T mod(int left, T right) { return this.mod(this.from(left), right); }
    default T mod(long left, T right) { return this.mod(this.from(left), right); }
    default T mod(float left, T right) { return this.mod(this.from(left), right); }
    default T mod(double left, T right) { return this.mod(this.from(left), right); }
    default <U, V> T mod(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.mod(this.from(leftType, left), this.from(rightType, right));
    }
    T negate(T value);

    // Comparison operations
    int compareTo(T left, T right);
    boolean lt(T left, T right);
    default <U> boolean lt(T left, DataCalculator<U> type, U right) { return this.lt(left, this.from(type, right)); }
    default boolean lt(T left, int right) { return this.lt(left, this.from(right)); }
    default boolean lt(T left, long right) { return this.lt(left, this.from(right)); }
    default boolean lt(T left, float right) { return this.lt(left, this.from(right)); }
    default boolean lt(T left, double right) { return this.lt(left, this.from(right)); }
    default <U> boolean lt(DataCalculator<U> type, U left, T right) { return this.lt(this.from(type, left), right); }
    default boolean lt(int left, T right) { return this.lt(this.from(left), right); }
    default boolean lt(long left, T right) { return this.lt(this.from(left), right); }
    default boolean lt(float left, T right) { return this.lt(this.from(left), right); }
    default boolean lt(double left, T right) { return this.lt(this.from(left), right); }
    default <U, V> boolean lt(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.lt(this.from(leftType, left), this.from(rightType, right));
    }
    boolean le(T left, T right);
    default <U> boolean le(T left, DataCalculator<U> type, U right) { return this.le(left, this.from(type, right)); }
    default boolean le(T left, int right) { return this.le(left, this.from(right)); }
    default boolean le(T left, long right) { return this.le(left, this.from(right)); }
    default boolean le(T left, float right) { return this.le(left, this.from(right)); }
    default boolean le(T left, double right) { return this.le(left, this.from(right)); }
    default <U> boolean le(DataCalculator<U> type, U left, T right) { return this.le(this.from(type, left), right); }
    default boolean le(int left, T right) { return this.le(this.from(left), right); }
    default boolean le(long left, T right) { return this.le(this.from(left), right); }
    default boolean le(float left, T right) { return this.le(this.from(left), right); }
    default boolean le(double left, T right) { return this.le(this.from(left), right); }
    default <U, V> boolean le(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.le(this.from(leftType, left), this.from(rightType, right));
    }
    boolean gt(T left, T right);
    default <U> boolean gt(T left, DataCalculator<U> type, U right) { return this.gt(left, this.from(type, right)); }
    default boolean gt(T left, int right) { return this.gt(left, this.from(right)); }
    default boolean gt(T left, long right) { return this.gt(left, this.from(right)); }
    default boolean gt(T left, float right) { return this.gt(left, this.from(right)); }
    default boolean gt(T left, double right) { return this.gt(left, this.from(right)); }
    default <U> boolean gt(DataCalculator<U> type, U left, T right) { return this.gt(this.from(type, left), right); }
    default boolean gt(int left, T right) { return this.gt(this.from(left), right); }
    default boolean gt(long left, T right) { return this.gt(this.from(left), right); }
    default boolean gt(float left, T right) { return this.gt(this.from(left), right); }
    default boolean gt(double left, T right) { return this.gt(this.from(left), right); }
    default <U, V> boolean gt(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.gt(this.from(leftType, left), this.from(rightType, right));
    }
    boolean ge(T left, T right);
    default <U> boolean ge(T left, DataCalculator<U> type, U right) { return this.ge(left, this.from(type, right)); }
    default boolean ge(T left, int right) { return this.ge(left, this.from(right)); }
    default boolean ge(T left, long right) { return this.ge(left, this.from(right)); }
    default boolean ge(T left, float right) { return this.ge(left, this.from(right)); }
    default boolean ge(T left, double right) { return this.ge(left, this.from(right)); }
    default <U> boolean ge(DataCalculator<U> type, U left, T right) { return this.ge(this.from(type, left), right); }
    default boolean ge(int left, T right) { return this.ge(this.from(left), right); }
    default boolean ge(long left, T right) { return this.ge(this.from(left), right); }
    default boolean ge(float left, T right) { return this.ge(this.from(left), right); }
    default boolean ge(double left, T right) { return this.ge(this.from(left), right); }
    default <U, V> boolean ge(DataCalculator<U> leftType, U left, DataCalculator<V> rightType, V right) {
        return this.ge(this.from(leftType, left), this.from(rightType, right));
    }

    // Math functions
    T abs(T value);
    default <U> T abs(DataCalculator<U> type, U value) { return this.abs(this.from(type, value)); }
    T floor(T value);
    default <U> T floor(DataCalculator<U> type, U value) { return this.floor(this.from(type, value)); }
    T sqrt(T value);
    default <U> T sqrt(DataCalculator<U> type, U value) { return this.sqrt(this.from(type, value)); }

    // Bitwise operations
    T and(T left, T right);
    default <U> T and(T left, DataCalculator<U> type, U right) { return this.and(left, this.from(type, right)); }
    default T and(T left, int right) { return this.and(left, this.from(right)); }
    default T and(T left, long right) { return this.and(left, this.from(right)); }
    default <U> T and(DataCalculator<U> type, U left, T right) { return this.and(this.from(type, left), right); }
    default T and(int left, T right) { return this.and(this.from(left), right); }
    default T and(long left, T right) { return this.and(this.from(left), right); }
    T or(T left, T right);
    default <U> T or(T left, DataCalculator<U> type, U right) { return this.or(left, this.from(type, right)); }
    default T or(T left, int right) { return this.or(left, this.from(right)); }
    default T or(T left, long right) { return this.or(left, this.from(right)); }
    default <U> T or(DataCalculator<U> type, U left, T right) { return this.or(this.from(type, left), right); }
    default T or(int left, T right) { return this.or(this.from(left), right); }
    default T or(long left, T right) { return this.or(this.from(left), right); }
    T xor(T left, T right);
    default <U> T xor(T left, DataCalculator<U> type, U right) { return this.xor(left, this.from(type, right)); }
    default T xor(T left, int right) { return this.xor(left, this.from(right)); }
    default T xor(T left, long right) { return this.xor(left, this.from(right)); }
    default <U> T xor(DataCalculator<U> type, U left, T right) { return this.xor(this.from(type, left), right); }
    default T xor(int left, T right) { return this.xor(this.from(left), right); }
    default T xor(long left, T right) { return this.xor(this.from(left), right); }
    T not(T value);
    T shl(T value, int shift);
    T shr(T value, int shift);

    // General conversions
    String toHexString(T value);

    // Number conversions (incoming)
    <U> T from(DataCalculator<U> type, U value);
    default <U extends CppNumber<U>> T from(U value) { return this.from(value.getType(), value); }
    T from(int value);
    T from(long value);
    T from(float value);
    T from(double value);

    // Number conversions (outgoing)
    boolean toBoolean(T value);
    byte toByte(T value);
    UByte toUByte(T value);
    short toShort(T value);
    UShort toUShort(T value);
    int toInt(T value);
    UInt toUInt(T value);
    long toLong(T value);
    ULong toULong(T value);
    float toFloat(T value);
    double toDouble(T value);
}
