package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Represents a data type in C++.
 * @param <T> The Java type that corresponds to the C++ type
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class DataType<T> {

    public static final DataType<Byte> INT8 = new CppInt8("INT8");
    public static final DataType<Short> UINT8 = new CppUInt8("UINT8");
    public static final DataType<Short> INT16 = new CppInt16("INT16");
    public static final DataType<Integer> UINT16 = new CppUInt16("UINT16");
    public static final DataType<Integer> INT32 = new CppInt32("INT32");
    public static final DataType<Long> UINT32 = new CppUInt32("UINT32");
    public static final DataType<Long> INT64 = new CppInt64("INT64");
    public static final DataType<BigInteger> UINT64 = new CppUInt64("UINT64");
    public static final DataType<Float> FLOAT32 = new CppFloat32("FLOAT32");
    public static final DataType<Double> FLOAT64 = new CppFloat64("FLOAT64");
    public static final DataType<Boolean> BOOL = new CppBool("BOOL");

    public static DataType<byte[]> bytes(int byteSize) {
        return new CppBytes(byteSize);
    }
    public static DataType<byte[]> bytes(long byteSize) {
        return new CppBytes((int) byteSize);
    }
    public static DataType<String> string(int byteSize) {
        return new CppUtf8String(byteSize);
    }

    @Getter private final String name;
    /** Byte size. */
    private final int size; // DataTypeLength()
    @Getter private final boolean integral, floatingPoint, signed;
    protected final T lowest, min, max; // Why, C++... Why make "lowest"...
    private final ByteBufGetter<T> getter;
    private final ByteBufSetter<T> setter;
    private final Function<Integer, T[]> arrayMaker;

    public int size() {
        return this.size;
    }
    public boolean isUnsigned() {
        return !this.signed;
    }

    /**
     * Returns the lowest finite value representable by the numeric type, that is,
     * a finite value {@code x} such that there is no other finite value {@code y} where {@code y < x}.
     * @implNote {@code std::numeric_limits<T>::lowest()}
     */
    public T lowest() { return this.lowest; }

    /**
     * Returns the minimum finite value representable by the numeric type.<br>
     * For floating-point types with denormalization, min returns the minimum positive normalized value.<br>
     * To find the value that has no values less than it, use {@link DataType#lowest()}.
     * @implNote {@code std::numeric_limits<T>::min()}
     */
    public T min() { return this.min; }

    /**
     * Returns the maximum finite value representable by the numeric type.
     * Meaningful for all bounded types.
     * @implNote {@code std::numeric_limits<T>::max()}
     */
    public T max() { return this.max; }

    public T getBuf(DataBuffer buffer, int byteIndex) {
        return getter.get(buffer.wrappedBuf(byteIndex, this.size), 0);
    }
    public void setBuf(DataBuffer buffer, int byteIndex, T value) {
        setter.set(buffer.wrappedBuf(byteIndex, this.size), 0, value);
    }

    public T[] newArray(int size) {
        return arrayMaker.apply(size);
    }

    public <U> boolean lt(T left, U right) { return this.compare(left, right) <  0; }
    public <U> boolean le(T left, U right) { return this.compare(left, right) <= 0; }
    public <U> boolean gt(T left, U right) { return this.compare(left, right) >  0; }
    public <U> boolean ge(T left, U right) { return this.compare(left, right) >= 0; }

    /** Works the same as the C++ version of {@code std::make_signed<T>::type} */
    public abstract <U> DataType<U> getSignedType();
    /** Works the same as the C++ version of {@code std::make_unsigned<T>::type} */
    public abstract <U> DataType<U> getUnsignedType();

    public abstract <U> T staticCast(U value);
    public abstract T unsafeCast(Object value);
    protected abstract <U> int compare(T left, U right);
    public abstract boolean equals(T left, T right);
    public abstract String toString(T value);
    public abstract T parse(String value);
    // Arithmetic operations
    public abstract T add(T left, T right);
    public abstract T subtract(T left, T right);
    public abstract T multiply(T left, T right);
    public abstract T divide(T left, T right);
    public abstract T mod(T left, T right);
    public abstract T negate(T value);
    // Math functions
    public abstract T abs(T value);
    public abstract T floor(T value);
    public abstract T sqrt(T value);
    // Bitwise operations
    public abstract T and(T left, T right);
    public abstract T or(T left, T right);
    public abstract T xor(T left, T right);
    public abstract T not(T value);
    public abstract T shiftLeft(T value, int shift);
    public abstract T shiftArithRight(T value, int shift);
    public abstract T shiftLogicRight(T value, int shift);

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static class CppInt8 extends JavaByte {
        private CppInt8(String name) {
            super(name, Byte.BYTES, true, (byte) -128, (byte) 127, (byte) 0xFF,
                    ByteBuf::getByte, ByteBuf::setByte);
        }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(UINT8); }
    }
    private static class CppUInt8 extends JavaShort {
        private CppUInt8(String name) {
            super(name, Byte.BYTES, false, (short) 0, (short) 255, (short) 0xFF,
                    ByteBuf::getUnsignedByte,
                    (buf, index, value) -> buf.setByte(index, (byte) (short) value));
        }
        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(INT8); }
    }
    private static class CppInt16 extends JavaShort {
        private CppInt16(String name) {
            super(name, Short.BYTES, true, (short) -32768, (short) 32767, (short) 0xFFFF,
                    ByteBuf::getShortLE, ByteBuf::setShortLE);
        }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(UINT16); }
    }
    private static class CppUInt16 extends JavaInteger {
        private CppUInt16(String name) {
            super(name, Short.BYTES, false, 0, 65535, 0xFFFF,
                    ByteBuf::getUnsignedShortLE,
                    (buf, index, value) -> buf.setShortLE(index, (short) (int) value));
        }
        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(INT16); }
    }
    private static class CppInt32 extends JavaInteger {
        private CppInt32(String name) {
            super(name, Integer.BYTES, true, Integer.MIN_VALUE, Integer.MAX_VALUE, 0xFFFFFFFF,
                    ByteBuf::getIntLE, ByteBuf::setIntLE);
        }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(UINT32); }
    }
    private static class CppUInt32 extends JavaLong {
        private CppUInt32(String name) {
            super(name, Integer.BYTES, false, 0L, 4294967295L, 0xFFFFFFFFL,
                    ByteBuf::getUnsignedIntLE,
                    (buf, index, value) -> buf.setIntLE(index, (int) (long) value));
        }
        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(INT32); }
    }
    private static class CppInt64 extends JavaLong {
        private CppInt64(String name) {
            super(name, Long.BYTES, true, Long.MIN_VALUE, Long.MAX_VALUE, 0xFFFFFFFFFFFFFFFFL,
                    ByteBuf::getLongLE, ByteBuf::setLongLE);
        }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(UINT64); }
    }
    private static class CppUInt64 extends JavaBigInteger {
        private static final BigInteger ULONG_MASK = BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE);
        private CppUInt64(String name) {
            super(name, Long.BYTES, false, BigInteger.ZERO, ULONG_MASK, ULONG_MASK,
                    (buf, index) -> JavaBigInteger.longToBigInteger(buf.getLongLE(index)),
                    (buf, index, value) -> buf.setLongLE(index, value.longValue()));
        }
        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(INT64); }
    }
    private static class CppFloat32 extends JavaFloat {
        private CppFloat32(String name) {
            super(name, 4, ByteBuf::getFloat, ByteBuf::setFloat);
        }
    }
    private static class CppFloat64 extends JavaDouble {
        private CppFloat64(String name) {
            super(name, 8, ByteBuf::getDouble, ByteBuf::setDouble);
        }
    }
    private static class CppBool extends JavaBoolean {
        private CppBool(String name) {
            super(name, 1, false, false, true, ByteBuf::getBoolean, ByteBuf::setBoolean);
        }
    }
    private static class CppBytes extends JavaByteArray {
        private CppBytes(int byteSize) {
            super("BYTES", byteSize,
                    (buf, index) -> { byte[] result = new byte[byteSize]; buf.getBytes(index, result); return result; },
                    ByteBuf::setBytes);
        }
    }
    private static class CppUtf8String extends JavaString {
        private CppUtf8String(int size) {
            super("UTF8_STR", size,
                    (buf, index) -> buf.getCharSequence(index, size, StandardCharsets.UTF_8).toString(),
                    (buf, index, value) -> buf.setCharSequence(index, value, StandardCharsets.UTF_8));
        }
    }

    private static class JavaByte extends JavaIntegralType<Byte> {
        private JavaByte(String name, int byteSize, boolean signed, Byte min, Byte max, Byte mask,
                         ByteBufGetter<Byte> getter, ByteBufSetter<Byte> setter) {
            super(name, byteSize, signed, min, max, mask, getter, setter, Byte[]::new);
        }

        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(this); }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(this); }

        public Byte unsafeCast(Object value) { return (Byte) value; }
        public <U> int compare(Byte left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Byte staticCast(U value) {
            byte result;
            if     (value instanceof Number ) result = ((Number) value).byteValue();
            else if(value instanceof Boolean) result = (byte) ((Boolean) value ? 1 : 0);
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public String toString(Byte value) { return value.toString(); }
        public Byte parse(String value) { return this.circularClamp(Byte.parseByte(value)); }
        // Arithmetic operations
        public Byte add(Byte left, Byte right) { return this.circularClamp(left + right); }
        public Byte subtract(Byte left, Byte right) { return this.circularClamp(left - right); }
        public Byte multiply(Byte left, Byte right) { return this.circularClamp(left * right); }
        public Byte divide(Byte left, Byte right) { return this.circularClamp(left / right); }
        public Byte mod(Byte left, Byte right) { return this.circularClamp(left % right); }
        public Byte negate(Byte value) { return this.circularClamp(-value); }
        // Math functions
        public Byte abs(Byte value) { return this.circularClamp(Math.abs(value)); }
        public Byte sqrt(Byte value) { return this.circularClamp((byte) Math.sqrt(value)); }
        // Bitwise operations
        public Byte and(Byte left, Byte right) { return (byte) (left & right); }
        public Byte or(Byte left, Byte right) { return (byte) (left | right); }
        public Byte xor(Byte left, Byte right) { return (byte) (left ^ right); }
        public Byte not(Byte value) { return (byte) ~value; }
        public Byte shiftLeft(Byte value, int shift) { return this.shiftClamp(value << shift); }
        public Byte shiftArithRight(Byte value, int shift) { return this.shiftClamp(value >> shift); }
        public Byte shiftLogicRight(Byte value, int shift) { return this.shiftClamp(value >>> shift); }
        public boolean equals(Byte left, Byte right) { return left.equals(right); }
        private byte circularClamp(int value) {
            int range = max - min + 1;
            return (byte) (((((value - min) % range) + range) % range) + min);
        }
        private byte shiftClamp(int value) {
            return (byte) (value & this.mask());
        }
    }

    private static class JavaShort extends JavaIntegralType<Short> {
        private JavaShort(String name, int byteSize, boolean signed, Short min, Short max, Short mask,
                          ByteBufGetter<Short> getter, ByteBufSetter<Short> setter) {
            super(name, byteSize, signed, min, max, mask, getter, setter, Short[]::new);
        }

        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(this); }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(this); }

        public Short unsafeCast(Object value) { return (Short) value; }
        public <U> int compare(Short left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Short staticCast(U value) {
            short result;
            if     (value instanceof Number ) result = ((Number) value).shortValue();
            else if(value instanceof Boolean) result = (short) ((Boolean) value ? 1 : 0);
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public String toString(Short value) { return value.toString(); }
        public Short parse(String value) { return this.circularClamp(Short.parseShort(value)); }
        // Arithmetic operations
        public Short add(Short left, Short right) { return this.circularClamp(left + right); }
        public Short subtract(Short left, Short right) { return this.circularClamp(left - right); }
        public Short multiply(Short left, Short right) { return this.circularClamp(left * right); }
        public Short divide(Short left, Short right) { return this.circularClamp(left / right); }
        public Short mod(Short left, Short right) { return this.circularClamp(left % right); }
        public Short negate(Short value) { return this.circularClamp(-value); }
        // Math functions
        public Short abs(Short value) { return this.circularClamp(Math.abs(value)); }
        public Short sqrt(Short value) { return this.circularClamp((short) Math.sqrt(value)); }
        // Bitwise operations
        public Short and(Short left, Short right) { return (short) (left & right); }
        public Short or(Short left, Short right) { return (short) (left | right); }
        public Short xor(Short left, Short right) { return (short) (left ^ right); }
        public Short not(Short value) { return (short) ~value; }
        public Short shiftLeft(Short value, int shift) { return this.shiftClamp(value << shift); }
        public Short shiftArithRight(Short value, int shift) { return this.shiftClamp(value >> shift); }
        public Short shiftLogicRight(Short value, int shift) { return this.shiftClamp(value >>> shift); }
        public boolean equals(Short left, Short right) { return left.equals(right); }
        private short circularClamp(int value) {
            int range = max - min + 1;
            return (short) (((((value - min) % range) + range) % range) + min);
        }
        private short shiftClamp(int value) {
            return (short) (value & this.mask());
        }
    }

    private static class JavaInteger extends JavaIntegralType<Integer> {
        private JavaInteger(String name, int byteSize, boolean signed, Integer min, Integer max, Integer mask,
                            ByteBufGetter<Integer> getter, ByteBufSetter<Integer> setter) {
            super(name, byteSize, signed, min, max, mask, getter, setter, Integer[]::new);
        }

        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(this); }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(this); }

        public Integer unsafeCast(Object value) { return (Integer) value; }
        public <U> int compare(Integer left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Integer staticCast(U value) {
            int result;
            if     (value instanceof Number ) result = ((Number) value).intValue();
            else if(value instanceof Boolean) result = (Boolean) value ? 1 : 0;
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public String toString(Integer value) { return value.toString(); }
        public Integer parse(String value) { return this.circularClamp(Integer.parseInt(value)); }
        // Arithmetic operations
        public Integer add(Integer left, Integer right) { return this.circularClamp(left + right); }
        public Integer subtract(Integer left, Integer right) { return this.circularClamp(left - right); }
        public Integer multiply(Integer left, Integer right) { return this.circularClamp(left * right); }
        public Integer divide(Integer left, Integer right) { return this.circularClamp(left / right); }
        public Integer mod(Integer left, Integer right) { return this.circularClamp(left % right); }
        public Integer negate(Integer value) { return this.circularClamp(-value); }
        // Math functions
        public Integer abs(Integer value) { return this.circularClamp(Math.abs(value)); }
        public Integer sqrt(Integer value) { return this.circularClamp((int) Math.sqrt(value)); }
        // Bitwise operations
        public Integer and(Integer left, Integer right) { return left & right; }
        public Integer or(Integer left, Integer right) { return left | right; }
        public Integer xor(Integer left, Integer right) { return left ^ right; }
        public Integer not(Integer value) { return ~value; }
        public Integer shiftLeft(Integer value, int shift) { return this.shiftClamp(value << shift); }
        public Integer shiftArithRight(Integer value, int shift) { return this.shiftClamp(value >> shift); }
        public Integer shiftLogicRight(Integer value, int shift) { return this.shiftClamp(value >>> shift); }
        public boolean equals(Integer left, Integer right) { return left.equals(right); }
        private int circularClamp(int value) {
            long range = (long) max - min + 1;
            return (int) (((((value - min) % range) + range) % range) + min);
        }
        private int shiftClamp(int value) {
            return value & this.mask();
        }
    }

    private static class JavaLong extends JavaIntegralType<Long> {
        private final BigInteger bigIntRange, bigIntMin;

        private JavaLong(String name, int byteSize, boolean signed, Long min, Long max, Long mask,
                         ByteBufGetter<Long> getter, ByteBufSetter<Long> setter) {
            super(name, byteSize, signed, min, max, mask, getter, setter, Long[]::new);
            BigInteger bigIntMax = BigInteger.valueOf(max);
            this.bigIntMin = BigInteger.valueOf(min);
            this.bigIntRange = bigIntMax.subtract(this.bigIntMin).add(BigInteger.ONE);
        }

        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(this); }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(this); }

        public Long unsafeCast(Object value) { return (Long) value; }
        public <U> int compare(Long left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Long staticCast(U value) {
            long result;
            if     (value instanceof Number ) result = ((Number) value).longValue();
            else if(value instanceof Boolean) result = (Boolean) value ? 1L : 0L;
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public String toString(Long value) { return value.toString(); }
        public Long parse(String value) { return this.circularClamp(Long.parseLong(value)); }
        // Arithmetic operations
        public Long add(Long left, Long right) { return this.circularClamp(left + right); }
        public Long subtract(Long left, Long right) { return this.circularClamp(left - right); }
        public Long multiply(Long left, Long right) { return this.circularClamp(left * right); }
        public Long divide(Long left, Long right) { return this.circularClamp(left / right); }
        public Long mod(Long left, Long right) { return this.circularClamp(left % right); }
        public Long negate(Long value) { return this.circularClamp(-value); }
        // Math functions
        public Long abs(Long value) { return this.circularClamp(Math.abs(value)); }
        public Long sqrt(Long value) { return this.circularClamp((long) Math.sqrt(value)); }
        // Bitwise operations
        public Long and(Long left, Long right) { return left & right; }
        public Long or(Long left, Long right) { return left | right; }
        public Long xor(Long left, Long right) { return left ^ right; }
        public Long not(Long value) { return ~value; }
        public Long shiftLeft(Long value, int shift) { return this.shiftClamp(value << shift); }
        public Long shiftArithRight(Long value, int shift) { return this.shiftClamp(value >> shift); }
        public Long shiftLogicRight(Long value, int shift) { return this.shiftClamp(value >>> shift); }
        public boolean equals(Long left, Long right) { return left.equals(right); }
        private long circularClamp(long value) {
            return BigInteger.valueOf(value)
                    .subtract(this.bigIntMin)
                    .mod(this.bigIntRange)
                    .add(this.bigIntRange)
                    .mod(this.bigIntRange)
                    .add(this.bigIntMin)
                    .longValue();
        }
        private long shiftClamp(long value) {
            return value & this.mask();
        }

        private static int integralCompare(long left, Object right) {
            if     (right instanceof Float     ) return Float.compare(left, (Float) right);
            else if(right instanceof Double    ) return Double.compare(left, (Double) right);
            else if(right instanceof BigInteger) return BigInteger.valueOf(left).compareTo((BigInteger) right);
            else if(right instanceof Number    ) return Long.compare(left, ((Number) right).longValue());
            else if(right instanceof Boolean   ) return (int) Math.signum(left - (((Boolean) right) ? 1 : 0));
            else throw new IllegalArgumentException("cannot compare integral type with " + right.getClass());
        }
    }

    private static class JavaBigInteger extends JavaIntegralType<BigInteger> {
        private final BigInteger bigIntRange, bigIntMin;

        private JavaBigInteger(String name, int byteSize, boolean signed, BigInteger min, BigInteger max, BigInteger mask,
                               ByteBufGetter<BigInteger> getter, ByteBufSetter<BigInteger> setter) {
            super(name, byteSize, signed, min, max, mask, getter, setter, BigInteger[]::new);
            this.bigIntMin = min;
            this.bigIntRange = max.subtract(min).add(BigInteger.ONE);
        }

        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(this); }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(this); }

        public BigInteger unsafeCast(Object value) { return (BigInteger) value; }
        public <U> int compare(BigInteger left, U right) {
            if     (right instanceof Float     ) return new BigDecimal(left).compareTo(BigDecimal.valueOf((Float) right));
            else if(right instanceof Double    ) return new BigDecimal(left).compareTo(BigDecimal.valueOf((Double) right));
            else if(right instanceof BigInteger) return left.compareTo((BigInteger) right);
            else if(right instanceof Number    ) return left.compareTo(BigInteger.valueOf(((Number) right).longValue()));
            else if(right instanceof Boolean   ) return left.compareTo(BigInteger.valueOf((Boolean) right ? 1 : 0));
            else throw new IllegalArgumentException("cannot compare integral type with " + right.getClass());
        }
        public <U> BigInteger staticCast(U value) {
            BigInteger result;
            if     (value instanceof Float     ) result = BigDecimal.valueOf((Float) value).toBigInteger();
            else if(value instanceof Double    ) result = BigDecimal.valueOf((Double) value).toBigInteger();
            else if(value instanceof BigInteger) result = (BigInteger) value;
            else if(value instanceof Number    ) result = BigInteger.valueOf(((Number) value).longValue());
            else if(value instanceof Boolean   ) result = (Boolean) value ? BigInteger.ONE : BigInteger.ZERO;
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public String toString(BigInteger value) { return value.toString(); }
        public BigInteger parse(String value) { return this.circularClamp(new BigInteger(value)); }
        // Arithmetic operations
        public BigInteger add(BigInteger left, BigInteger right) { return this.circularClamp(left.add(right)); }
        public BigInteger subtract(BigInteger left, BigInteger right) { return this.circularClamp(left.subtract(right)); }
        public BigInteger multiply(BigInteger left, BigInteger right) { return this.circularClamp(left.multiply(right)); }
        public BigInteger divide(BigInteger left, BigInteger right) { return this.circularClamp(left.divide(right)); }
        public BigInteger mod(BigInteger left, BigInteger right) { return this.circularClamp(left.mod(right)); }
        public BigInteger negate(BigInteger value) { return this.circularClamp(value.negate()); }
        // Math functions
        public BigInteger abs(BigInteger value) { return this.circularClamp(value.abs()); }
        // Convert to double using doubleValue() and then back to BigInteger to get the square root
        // ps. WHY???? WHY IS SQRT IN BIGINTEGER SINCE JAVA 9?????
        public BigInteger sqrt(BigInteger value) { return this.circularClamp(BigInteger.valueOf((long) Math.sqrt(value.doubleValue()))); }
        // Bitwise operations
        public BigInteger and(BigInteger left, BigInteger right) { return left.and(right); }
        public BigInteger or(BigInteger left, BigInteger right) { return left.or(right); }
        public BigInteger xor(BigInteger left, BigInteger right) { return left.xor(right); }
        public BigInteger not(BigInteger value) { return value.not(); }
        public BigInteger shiftLeft(BigInteger value, int shift) { return this.shiftClamp(value.shiftLeft(shift)); }
        public BigInteger shiftArithRight(BigInteger value, int shift) { return this.shiftClamp(value.shiftRight(shift)); }
        public BigInteger shiftLogicRight(BigInteger value, int shift) { return this.shiftClamp(value.shiftRight(shift)); }
        public boolean equals(BigInteger left, BigInteger right) { return left.equals(right); }
        private BigInteger circularClamp(BigInteger value) {
            return value.subtract(this.bigIntMin)
                    .mod(this.bigIntRange)
                    .add(this.bigIntRange)
                    .mod(this.bigIntRange)
                    .add(this.bigIntMin);
        }
        private BigInteger shiftClamp(BigInteger value) {
            return value.and(this.mask());
        }

        private static BigInteger longToBigInteger(long value) {
            int upper = (int) (value >>> 32);
            int lower = (int) value;
            return (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                    add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
        }
    }

    private static class JavaFloat extends DataType<Float> {
        private JavaFloat(String name, int byteSize, ByteBufGetter<Float> getter, ByteBufSetter<Float> setter) {
            super(name, byteSize, false, true, false, -Float.MAX_VALUE, Float.MIN_NORMAL, Float.MAX_VALUE,
                    getter, setter, Float[]::new);
        }

        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(this); }
        public <U> DataType<U> getUnsignedType() { throw new UnsupportedOperationException(); }

        public Float unsafeCast(Object value) { return (Float) value; }
        public <U> int compare(Float left, U right) { return JavaDouble.floatingPointCompare(left, right); }
        public <U> Float staticCast(U value) {
            if     (value instanceof Number ) return ((Number) value).floatValue();
            else if(value instanceof Boolean) return (Boolean) value ? 1f : 0f;
            else throw new IllegalArgumentException();
        }
        public String toString(Float value) { return value.toString(); }
        public Float parse(String value) { return Float.parseFloat(value); }
        // Arithmetic operations
        public Float add(Float left, Float right) { return left + right; }
        public Float subtract(Float left, Float right) { return left - right; }
        public Float multiply(Float left, Float right) { return left * right; }
        public Float divide(Float left, Float right) { return left / right; }
        public Float mod(Float left, Float right) { return left % right; }
        public Float negate(Float value) { return -value; }
        // Math functions
        public Float abs(Float value) { return Math.abs(value); }
        public Float sqrt(Float value) { return (float) Math.sqrt(value); }
        public Float floor(Float value) { return (float) Math.floor(value); }
        // Bitwise operations
        public Float and(Float left, Float right) { throw new UnsupportedOperationException(); }
        public Float or(Float left, Float right) { throw new UnsupportedOperationException(); }
        public Float xor(Float left, Float right) { throw new UnsupportedOperationException(); }
        public Float not(Float value) { throw new UnsupportedOperationException(); }
        public Float shiftLeft(Float value, int shift) { throw new UnsupportedOperationException(); }
        public Float shiftArithRight(Float value, int shift) { throw new UnsupportedOperationException(); }
        public Float shiftLogicRight(Float value, int shift) { throw new UnsupportedOperationException(); }
        public boolean equals(Float left, Float right) { return left.equals(right); }
    }

    private static class JavaDouble extends DataType<Double> {
        private JavaDouble(String name, int byteSize, ByteBufGetter<Double> getter, ByteBufSetter<Double> setter) {
            super(name, byteSize, false, true, false, -Double.MAX_VALUE, Double.MIN_NORMAL, Double.MAX_VALUE,
                    getter, setter, Double[]::new);
        }

        public <U> DataType<U> getSignedType() { return BTRUtil.uncheckedCast(this); }
        public <U> DataType<U> getUnsignedType() { throw new UnsupportedOperationException(); }

        public Double unsafeCast(Object value) { return (Double) value; }
        public <U> int compare(Double left, U right) { return JavaDouble.floatingPointCompare(left, right); }
        public <U> Double staticCast(U value) {
            if     (value instanceof Number) return ((Number) value).doubleValue();
            else if(value instanceof Boolean) return (Boolean) value ? 1d : 0d;
            else throw new IllegalArgumentException();
        }
        public String toString(Double value) { return value.toString(); }
        public Double parse(String value) { return Double.parseDouble(value); }
        // Arithmetic operations
        public Double add(Double left, Double right) { return left + right; }
        public Double subtract(Double left, Double right) { return left - right; }
        public Double multiply(Double left, Double right) { return left * right; }
        public Double divide(Double left, Double right) { return left / right; }
        public Double mod(Double left, Double right) { return left % right; }
        public Double negate(Double value) { return -value; }
        // Math functions
        public Double abs(Double value) { return Math.abs(value); }
        public Double sqrt(Double value) { return Math.sqrt(value); }
        public Double floor(Double value) { return Math.floor(value); }
        // Bitwise operations
        public Double and(Double left, Double right) { throw new UnsupportedOperationException(); }
        public Double or(Double left, Double right) { throw new UnsupportedOperationException(); }
        public Double xor(Double left, Double right) { throw new UnsupportedOperationException(); }
        public Double not(Double value) { throw new UnsupportedOperationException(); }
        public Double shiftLeft(Double value, int shift) { throw new UnsupportedOperationException(); }
        public Double shiftArithRight(Double value, int shift) { throw new UnsupportedOperationException(); }
        public Double shiftLogicRight(Double value, int shift) { throw new UnsupportedOperationException(); }
        public boolean equals(Double left, Double right) { return left.equals(right); }

        private static int floatingPointCompare(double left, Object right) {
            if     (right instanceof BigInteger) return BigDecimal.valueOf(left).compareTo(new BigDecimal((BigInteger) right));
            else if(right instanceof Number    ) return Double.compare(left, ((Number) right).doubleValue());
            else if(right instanceof Boolean   ) return Double.compare(left, (Boolean) right ? 1 : 0);
            else throw new IllegalArgumentException("cannot compare float type with " + right.getClass());
        }
    }

    // false acts as 0, true acts as 1
    private static class JavaBoolean extends JavaIntegralType<Boolean> {
        private JavaBoolean(String name, int byteSize, boolean signed, Boolean min, Boolean max,
                            ByteBufGetter<Boolean> getter, ByteBufSetter<Boolean> setter) {
            super(name, byteSize, signed, min, max, true, getter, setter, Boolean[]::new);
        }

        public <U> DataType<U> getSignedType() { throw new UnsupportedOperationException(); }
        public <U> DataType<U> getUnsignedType() { return BTRUtil.uncheckedCast(this); }

        public Boolean unsafeCast(Object value) { return (Boolean) value; }
        public <U> int compare(Boolean left, U right) { return JavaLong.integralCompare(left ? 1 : 0, right); }
        public <U> Boolean staticCast(U value) {
            if     (value instanceof Number) return ((Number) value).intValue() != 0;
            else if(value instanceof Boolean) return (Boolean) value;
            else throw new IllegalArgumentException();
        }
        public String toString(Boolean value) { return value ? "1" : "0"; }
        public Boolean parse(String value) { return !value.equals("0"); }

        // Based on C++'s behaviour: "std::cout << (bool) (true op false) << std::endl"
        // Arithmetic operations
        public Boolean add(Boolean left, Boolean right) { return left | right; }
        public Boolean subtract(Boolean left, Boolean right) { return left ^ right; }
        public Boolean multiply(Boolean left, Boolean right) { return left & right; }
        public Boolean divide(Boolean left, Boolean right) {
            if(!right) throw new ArithmeticException("/ by zero");
            return left;
        }
        public Boolean mod(Boolean left, Boolean right) {
            if(!right) throw new ArithmeticException("/ by zero");
            return false;
        }
        public Boolean negate(Boolean value) { return !value; }
        // Math functions
        public Boolean abs(Boolean value) { return value; }
        public Boolean sqrt(Boolean value) { return value; }
        // Bitwise operations
        public Boolean and(Boolean left, Boolean right) { return left & right; }
        public Boolean or(Boolean left, Boolean right) { return left | right; }
        public Boolean xor(Boolean left, Boolean right) { return left ^ right; }
        public Boolean not(Boolean value) { return !value; }
        public Boolean shiftLeft(Boolean value, int shift) { return (value ? 1 : 0) << shift != 0; }
        public Boolean shiftArithRight(Boolean value, int shift) { return (value ? 1 : 0) >> shift != 0; }
        public Boolean shiftLogicRight(Boolean value, int shift) { return (value ? 1 : 0) >>> shift != 0; }
        public boolean equals(Boolean left, Boolean right) { return left.equals(right); }
    }

    private static abstract class JavaIntegralType<T> extends DataType<T> {
        private final T mask;
        private JavaIntegralType(String name, int byteSize, boolean signed, T min, T max, T mask,
                                 ByteBufGetter<T> getter, ByteBufSetter<T> setter,
                                 Function<Integer, T[]> arrayMaker) {
            super(name, byteSize, true, false, signed, min, min, max, getter, setter, arrayMaker);
            this.mask = mask;
        }
        public final T floor(T value) { return value; }
        public final T mask() { return mask; }
    }

    private static abstract class JavaString extends JavaBytesType<String> {
        private JavaString(String name, int byteSize, ByteBufGetter<String> getter, ByteBufSetter<String> setter) {
            super(name, byteSize, getter, setter, String[]::new);
        }

        public String unsafeCast(Object value) { return (String) value; }
        public <U> int compare(String left, U right) { return left.compareTo(right.toString()); }
        public <U> String staticCast(U value) { return value.toString(); }
        public String toString(String value) { return value; }
        public String parse(String value) { return value; }
        public String add(String left, String right) { return left + right; }
        public boolean equals(String left, String right) { return left.equals(right); }
    }

    private static abstract class JavaByteArray extends JavaBytesType<byte[]> {
        private JavaByteArray(String name, int byteSize, ByteBufGetter<byte[]> getter, ByteBufSetter<byte[]> setter) {
            super(name, byteSize, getter, setter, byte[][]::new);
        }

        public byte[] unsafeCast(Object value) { return (byte[]) value; }
        public <U> int compare(byte[] left, U right) { throw new UnsupportedOperationException(); }
        public <U> byte[] staticCast(U value) { return (byte[]) value; }
        public String toString(byte[] value) { return new String(value, StandardCharsets.UTF_8); }
        public byte[] parse(String value) { throw new UnsupportedOperationException(); }
        public byte[] add(byte[] left, byte[] right) { throw new UnsupportedOperationException(); }
        public boolean equals(byte[] left, byte[] right) { return Arrays.equals(left, right); }
    }

    private static abstract class JavaBytesType<T> extends DataType<T> {
        private JavaBytesType(String name, int byteSize, ByteBufGetter<T> getter, ByteBufSetter<T> setter,
                              Function<Integer, T[]> arrayMaker) {
            super(name, byteSize, false, false, false, null, null, null,
                    getter, setter, arrayMaker);
        }

        public <U> DataType<U> getSignedType() { throw new UnsupportedOperationException(); }
        public <U> DataType<U> getUnsignedType() { throw new UnsupportedOperationException(); }

        // Arithmetic operations
        public T subtract(T left, T right) { throw new UnsupportedOperationException(); }
        public T multiply(T left, T right) { throw new UnsupportedOperationException(); }
        public T divide(T left, T right) { throw new UnsupportedOperationException(); }
        public T mod(T left, T right) { throw new UnsupportedOperationException(); }
        public T negate(T value) { throw new UnsupportedOperationException(); }
        // Math functions
        public T abs(T value) { throw new UnsupportedOperationException(); }
        public T sqrt(T value) { throw new UnsupportedOperationException(); }
        public T floor(T value) { throw new UnsupportedOperationException(); }
        // Bitwise operations
        public T and(T left, T right) { throw new UnsupportedOperationException(); }
        public T or(T left, T right) { throw new UnsupportedOperationException(); }
        public T xor(T left, T right) { throw new UnsupportedOperationException(); }
        public T not(T value) { throw new UnsupportedOperationException(); }
        public T shiftLeft(T value, int shift) { throw new UnsupportedOperationException(); }
        public T shiftArithRight(T value, int shift) { throw new UnsupportedOperationException(); }
        public T shiftLogicRight(T value, int shift) { throw new UnsupportedOperationException(); }
    }

    @FunctionalInterface private interface ByteBufGetter<T> { T get(ByteBuf buf, int index); }
    @FunctionalInterface private interface ByteBufSetter<T> { void set(ByteBuf buf, int index, T value); }
}
