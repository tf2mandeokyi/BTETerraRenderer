package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.number.*;

public interface DataNumberType<T> extends DataType<T> {
    // Type conversions
    DataNumberType<?> getSigned();
    DataNumberType<?> getUnsigned();
    @SuppressWarnings("unused")
    default <U> DataNumberType<U> makeSigned() { return BTRUtil.uncheckedCast(this.getSigned()); }
    default <U> DataNumberType<U> makeUnsigned() { return BTRUtil.uncheckedCast(this.getUnsigned()); }
    
    // Number properties
    boolean isIntegral();
    boolean isSigned();
    default boolean isFloatingPoint() { return !isIntegral(); }
    default boolean isUnsigned() { return !this.isSigned(); }
    T lowest();
    T min();
    T max();
    byte getId();

    // Arithmetic operations
    T add(T left, T right);
    default T add(T left, int right) { return this.add(left, this.from(right)); }
    default T add(T left, long right) { return this.add(left, this.from(right)); }
    default T add(T left, float right) { return this.add(left, this.from(right)); }
    default T add(T left, double right) { return this.add(left, this.from(right)); }
    default T add(int left, T right) { return this.add(this.from(left), right); }
    default T add(long left, T right) { return this.add(this.from(left), right); }
    default T add(float left, T right) { return this.add(this.from(left), right); }
    default T add(double left, T right) { return this.add(this.from(left), right); }

    T sub(T left, T right);
    default T sub(T left, int right) { return this.sub(left, this.from(right)); }
    default T sub(T left, long right) { return this.sub(left, this.from(right)); }
    default T sub(T left, float right) { return this.sub(left, this.from(right)); }
    default T sub(T left, double right) { return this.sub(left, this.from(right)); }
    default T sub(int left, T right) { return this.sub(this.from(left), right); }
    default T sub(long left, T right) { return this.sub(this.from(left), right); }
    default T sub(float left, T right) { return this.sub(this.from(left), right); }
    default T sub(double left, T right) { return this.sub(this.from(left), right); }

    T mul(T left, T right);
    default T mul(T left, int right) { return this.mul(left, this.from(right)); }
    default T mul(T left, long right) { return this.mul(left, this.from(right)); }
    default T mul(T left, float right) { return this.mul(left, this.from(right)); }
    default T mul(T left, double right) { return this.mul(left, this.from(right)); }
    default T mul(int left, T right) { return this.mul(this.from(left), right); }
    default T mul(long left, T right) { return this.mul(this.from(left), right); }
    default T mul(float left, T right) { return this.mul(this.from(left), right); }
    default T mul(double left, T right) { return this.mul(this.from(left), right); }

    T div(T left, T right);
    default T div(T left, int right) { return this.div(left, this.from(right)); }
    default T div(T left, long right) { return this.div(left, this.from(right)); }
    default T div(T left, float right) { return this.div(left, this.from(right)); }
    default T div(T left, double right) { return this.div(left, this.from(right)); }
    default T div(int left, T right) { return this.div(this.from(left), right); }
    default T div(long left, T right) { return this.div(this.from(left), right); }
    default T div(float left, T right) { return this.div(this.from(left), right); }
    default T div(double left, T right) { return this.div(this.from(left), right); }

    T mod(T left, T right);
    default T mod(T left, int right) { return this.mod(left, this.from(right)); }
    default T mod(T left, long right) { return this.mod(left, this.from(right)); }
    default T mod(T left, float right) { return this.mod(left, this.from(right)); }
    default T mod(T left, double right) { return this.mod(left, this.from(right)); }
    default T mod(int left, T right) { return this.mod(this.from(left), right); }
    default T mod(long left, T right) { return this.mod(this.from(left), right); }
    default T mod(float left, T right) { return this.mod(this.from(left), right); }
    default T mod(double left, T right) { return this.mod(this.from(left), right); }

    T negate(T value);

    // Comparison operations
    boolean equals(T left, T right);
    default boolean equals(T left, int right) { return this.equals(left, this.from(right)); }
    default boolean equals(T left, long right) { return this.equals(left, this.from(right)); }
    default boolean equals(T left, float right) { return this.equals(left, this.from(right)); }
    default boolean equals(T left, double right) { return this.equals(left, this.from(right)); }
    default boolean equals(int left, T right) { return this.equals(this.from(left), right); }
    default boolean equals(long left, T right) { return this.equals(this.from(left), right); }
    default boolean equals(float left, T right) { return this.equals(this.from(left), right); }
    default boolean equals(double left, T right) { return this.equals(this.from(left), right); }

    int compareTo(T left, T right);

    boolean lt(T left, T right);
    default boolean lt(T left, int right) { return this.lt(left, this.from(right)); }
    default boolean lt(T left, long right) { return this.lt(left, this.from(right)); }
    default boolean lt(T left, float right) { return this.lt(left, this.from(right)); }
    default boolean lt(T left, double right) { return this.lt(left, this.from(right)); }
    default boolean lt(int left, T right) { return this.lt(this.from(left), right); }
    default boolean lt(long left, T right) { return this.lt(this.from(left), right); }
    default boolean lt(float left, T right) { return this.lt(this.from(left), right); }
    default boolean lt(double left, T right) { return this.lt(this.from(left), right); }

    boolean le(T left, T right);
    default boolean le(T left, int right) { return this.le(left, this.from(right)); }
    default boolean le(T left, long right) { return this.le(left, this.from(right)); }
    default boolean le(T left, float right) { return this.le(left, this.from(right)); }
    default boolean le(T left, double right) { return this.le(left, this.from(right)); }
    default boolean le(int left, T right) { return this.le(this.from(left), right); }
    default boolean le(long left, T right) { return this.le(this.from(left), right); }
    default boolean le(float left, T right) { return this.le(this.from(left), right); }
    default boolean le(double left, T right) { return this.le(this.from(left), right); }

    boolean gt(T left, T right);
    default boolean gt(T left, int right) { return this.gt(left, this.from(right)); }
    default boolean gt(T left, long right) { return this.gt(left, this.from(right)); }
    default boolean gt(T left, float right) { return this.gt(left, this.from(right)); }
    default boolean gt(T left, double right) { return this.gt(left, this.from(right)); }
    default boolean gt(int left, T right) { return this.gt(this.from(left), right); }
    default boolean gt(long left, T right) { return this.gt(this.from(left), right); }
    default boolean gt(float left, T right) { return this.gt(this.from(left), right); }
    default boolean gt(double left, T right) { return this.gt(this.from(left), right); }

    boolean ge(T left, T right);
    default boolean ge(T left, int right) { return this.ge(left, this.from(right)); }
    default boolean ge(T left, long right) { return this.ge(left, this.from(right)); }
    default boolean ge(T left, float right) { return this.ge(left, this.from(right)); }
    default boolean ge(T left, double right) { return this.ge(left, this.from(right)); }
    default boolean ge(int left, T right) { return this.ge(this.from(left), right); }
    default boolean ge(long left, T right) { return this.ge(this.from(left), right); }
    default boolean ge(float left, T right) { return this.ge(this.from(left), right); }
    default boolean ge(double left, T right) { return this.ge(this.from(left), right); }

    // Math functions
    T abs(T value);
    default <U> T abs(DataNumberType<U> type, U value) { return this.abs(this.from(type, value)); }
    T floor(T value);
    default <U> T floor(DataNumberType<U> type, U value) { return this.floor(this.from(type, value)); }
    T sqrt(T value);
    default <U> T sqrt(DataNumberType<U> type, U value) { return this.sqrt(this.from(type, value)); }

    // Bitwise operations
    T and(T left, T right);
    default T and(T left, int right) { return this.and(left, this.from(right)); }
    default T and(T left, long right) { return this.and(left, this.from(right)); }
    default T and(int left, T right) { return this.and(this.from(left), right); }
    default T and(long left, T right) { return this.and(this.from(left), right); }

    T or(T left, T right);
    default T or(T left, int right) { return this.or(left, this.from(right)); }
    default T or(T left, long right) { return this.or(left, this.from(right)); }
    default T or(int left, T right) { return this.or(this.from(left), right); }
    default T or(long left, T right) { return this.or(this.from(left), right); }

    T xor(T left, T right);
    default T xor(T left, int right) { return this.xor(left, this.from(right)); }
    default T xor(T left, long right) { return this.xor(left, this.from(right)); }
    default T xor(int left, T right) { return this.xor(this.from(left), right); }
    default T xor(long left, T right) { return this.xor(this.from(left), right); }

    T not(T value);
    T shl(T value, int shift);
    T shr(T value, int shift);

    // General conversions
    String toHexString(T value);

    // Number conversions (incoming)
    <U> T from(DataNumberType<U> type, U value);
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
