package com.mndk.bteterrarenderer.datatype.number;

import com.mndk.bteterrarenderer.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataNumberType;

import javax.annotation.Nonnull;

public abstract class CppNumber<T extends CppNumber<T>> extends Number implements Comparable<T> {

    public boolean equals(Object obj) {
        if (!(obj instanceof CppNumber)) return false;
        try {
            return this.equals(BTRUtil.uncheckedCast(obj));
        } catch (ClassCastException ignored) {
            return false;
        }
    }
    public abstract int hashCode();
    public abstract String toString();
    public abstract String toHexString();
    public abstract DataNumberType<T> getType();

    // #################### Arithmetic operations ####################

    public abstract T add(T other);
    public <U> T add(DataNumberType<U> type, U other) { return this.add(this.getType().from(type, other)); }
    public T add(int other) { return this.add(this.getType().from(other)); }
    public T add(long other) { return this.add(this.getType().from(other)); }
    public abstract T sub(T other);
    public <U> T sub(DataNumberType<U> type, U other) { return this.add(this.getType().from(type, other)); }
    public T sub(int other) { return this.sub(this.getType().from(other)); }
    public T sub(long other) { return this.sub(this.getType().from(other)); }
    public abstract T mul(T other);
    public <U> T mul(DataNumberType<U> type, U other) { return this.add(this.getType().from(type, other)); }
    public T mul(int other) { return this.mul(this.getType().from(other)); }
    public T mul(long other) { return this.mul(this.getType().from(other)); }
    public abstract T div(T other);
    public <U> T div(DataNumberType<U> type, U other) { return this.add(this.getType().from(type, other)); }
    public T div(int other) { return this.div(this.getType().from(other)); }
    public T div(long other) { return this.div(this.getType().from(other)); }
    public abstract T mod(T other);
    public <U> T mod(DataNumberType<U> type, U other) { return this.add(this.getType().from(type, other)); }
    public T mod(int other) { return this.mod(this.getType().from(other)); }
    public T mod(long other) { return this.mod(this.getType().from(other)); }
    public abstract T negate();

    // #################### Comparison operations ####################

    public abstract boolean equals(T other);
    public boolean equals(int other) { return this.equals(this.getType().from(other)); }
    public boolean equals(long other) { return this.equals(this.getType().from(other)); }
    public abstract int compareTo(@Nonnull T other);
    public int compareTo(int other) { return this.compareTo(this.getType().from(other)); }
    public int compareTo(long other) { return this.compareTo(this.getType().from(other)); }
    public boolean lt(T other) { return this.compareTo(other) <  0; }
    public boolean lt(int other) { return this.lt(this.getType().from(other)); }
    public boolean lt(long other) { return this.lt(this.getType().from(other)); }
    public boolean le(T other) { return this.compareTo(other) <= 0; }
    public boolean le(int other) { return this.le(this.getType().from(other)); }
    public boolean le(long other) { return this.le(this.getType().from(other)); }
    public boolean gt(T other) { return this.compareTo(other) >  0; }
    public boolean gt(int other) { return this.gt(this.getType().from(other)); }
    public boolean gt(long other) { return this.gt(this.getType().from(other)); }
    public boolean ge(T other) { return this.compareTo(other) >= 0; }
    public boolean ge(int other) { return this.ge(this.getType().from(other)); }
    public boolean ge(long other) { return this.ge(this.getType().from(other)); }

    // #################### Math functions ####################

    public abstract T abs();
    public abstract T floor();
    public abstract T sqrt();

    // #################### Bitwise operations ####################

    public abstract T and(T other);
    public T and(int other) { return this.and(this.getType().from(other)); }
    public T and(long other) { return this.and(this.getType().from(other)); }
    public abstract T or(T other);
    public T or(int other) { return this.or(this.getType().from(other)); }
    public T or(long other) { return this.or(this.getType().from(other)); }
    public abstract T xor(T other);
    public T xor(int other) { return this.xor(this.getType().from(other)); }
    public T xor(long other) { return this.xor(this.getType().from(other)); }
    public abstract T not();
    public abstract T shl(int shift);
    public abstract T shr(int shift);

    // #################### Type conversion ####################

    public abstract boolean booleanValue();
    public abstract byte byteValue();
    public abstract short shortValue();
    public abstract int intValue();
    public abstract long longValue();
    public abstract float floatValue();
    public abstract double doubleValue();
    public abstract UByte uByteValue();
    public abstract UShort uShortValue();
    public abstract UInt uIntValue();
    public abstract ULong uLongValue();
}
