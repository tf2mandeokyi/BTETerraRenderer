package com.mndk.bteterrarenderer.draco.core;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

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

    @Getter private final String name;
    @Getter private final int byteSize;
    @Getter private final boolean integral, floatingPoint, signed;
    protected final T min, max;
    private final ByteBufReader<T> reader;
    private final ByteBufGetter<T> getter;
    private final ByteBufWriter<T> writer;
    private final ByteBufSetter<T> setter;
    private final Function<Integer, T[]> arrayMaker;

    public T min() { return this.min; }
    public T max() { return this.max; }

    public T readBuf(ByteBuf buf) {
        return reader.read(buf);
    }

    public T getBuf(ByteBuf buf, int byteIndex) {
        return getter.get(buf, byteIndex);
    }

    public void writeBuf(ByteBuf buf, T value) {
        writer.write(buf, value);
    }

    public void setBuf(ByteBuf buf, int byteIndex, T value) {
        setter.set(buf, byteIndex, value);
    }

    public T[] newArray(int size) {
        return arrayMaker.apply(size);
    }

    public <U> boolean lt (T left, U right) { return this.compare(left, right) <  0; }
    public <U> boolean lte(T left, U right) { return this.compare(left, right) <= 0; }
    public <U> boolean gt (T left, U right) { return this.compare(left, right) >  0; }
    public <U> boolean gte(T left, U right) { return this.compare(left, right) >= 0; }

    public abstract T unsafeCast(Object value);
    protected abstract <U> int compare(T left, U right);
    public abstract T add(T left, T right);
    public abstract T subtract(T left, T right);
    public abstract T multiply(T left, T right);
    public abstract T divide(T left, T right);
    public abstract T mod(T left, T right);
    public abstract T floor(T value);
    public abstract <U> T staticCast(U value);

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    private static class CppInt8 extends JavaByte {
        private CppInt8(String name) {
            super(name, Byte.BYTES, true, (byte) -128, (byte) 127,
                    ByteBuf::readByte, ByteBuf::getByte,
                    ByteBuf::writeByte, ByteBuf::setByte);
        }
    }
    private static class CppUInt8 extends JavaShort {
        private CppUInt8(String name) {
            super(name, Byte.BYTES, false, (short) 0, (short) 255,
                    ByteBuf::readUnsignedByte, ByteBuf::getUnsignedByte,
                    (buf, value) -> buf.writeByte((byte) (short) value),
                    (buf, index, value) -> buf.setByte(index, (byte) (short) value));
        }
    }
    private static class CppInt16 extends JavaShort {
        private CppInt16(String name) {
            super(name, Short.BYTES, true, (short) -32768, (short) 32767,
                    ByteBuf::readShortLE, ByteBuf::getShortLE,
                    ByteBuf::writeShortLE, ByteBuf::setShortLE);
        }
    }
    private static class CppUInt16 extends JavaInteger {
        private CppUInt16(String name) {
            super(name, Short.BYTES, false, 0, 65535,
                    ByteBuf::readUnsignedShortLE, ByteBuf::getUnsignedShortLE,
                    (buf, value) -> buf.writeShortLE((short) (int) value),
                    (buf, index, value) -> buf.setShortLE(index, (short) (int) value));
        }
    }
    private static class CppInt32 extends JavaInteger {
        private CppInt32(String name) {
            super(name, Integer.BYTES, true, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    ByteBuf::readIntLE, ByteBuf::getIntLE,
                    ByteBuf::writeIntLE, ByteBuf::setIntLE);
        }
    }
    private static class CppUInt32 extends JavaLong {
        private CppUInt32(String name) {
            super(name, Integer.BYTES, false, 0L, 4294967295L,
                    ByteBuf::readUnsignedIntLE, ByteBuf::getUnsignedIntLE,
                    (buf, value) -> buf.writeIntLE((int) (long) value),
                    (buf, index, value) -> buf.setIntLE(index, (int) (long) value));
        }
    }
    private static class CppInt64 extends JavaLong {
        private CppInt64(String name) {
            super(name, Long.BYTES, true, Long.MIN_VALUE, Long.MAX_VALUE,
                    ByteBuf::readLongLE, ByteBuf::getLongLE,
                    ByteBuf::writeLongLE, ByteBuf::setLongLE);
        }
    }
    private static class CppUInt64 extends JavaBigInteger {
        private static final BigInteger ULONG_MASK = BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE);
        private CppUInt64(String name) {
            super(name, Long.BYTES, false, BigInteger.ZERO, ULONG_MASK,
                    (buf) -> JavaBigInteger.longToBigInteger(buf.readLongLE()),
                    (buf, index) -> JavaBigInteger.longToBigInteger(buf.getLongLE(index)),
                    (buf, value) -> buf.writeLongLE(value.longValue()),
                    (buf, index, value) -> buf.setLongLE(index, value.longValue()));
        }
    }
    private static class CppFloat32 extends JavaFloat {
        private CppFloat32(String name) {
            super(name, 4, true, -Float.MAX_VALUE, Float.MAX_VALUE,
                    ByteBuf::readFloat, ByteBuf::getFloat,
                    ByteBuf::writeFloat, ByteBuf::setFloat);
        }
    }
    private static class CppFloat64 extends JavaDouble {
        private CppFloat64(String name) {
            super(name, 8, true, -Double.MAX_VALUE, Double.MAX_VALUE,
                    ByteBuf::readDouble, ByteBuf::getDouble,
                    ByteBuf::writeDouble, ByteBuf::setDouble);
        }
    }
    private static class CppBool extends JavaBoolean {
        private CppBool(String name) {
            super(name, 1, false, false, true,
                    ByteBuf::readBoolean, ByteBuf::getBoolean,
                    ByteBuf::writeBoolean, ByteBuf::setBoolean);
        }
    }

    private static abstract class JavaByte extends JavaIntegralType<Byte> {
        private JavaByte(String name, int byteSize, boolean signed, Byte min, Byte max,
                         ByteBufReader<Byte> reader, ByteBufGetter<Byte> getter,
                         ByteBufWriter<Byte> writer, ByteBufSetter<Byte> setter) {
            super(name, byteSize, signed, min, max, reader, getter, writer, setter, Byte[]::new);
        }

        public Byte unsafeCast(Object value) { return (Byte) value; }
        public <U> int compare(Byte left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Byte staticCast(U value) {
            byte result;
            if     (value instanceof Number ) result = ((Number) value).byteValue();
            else if(value instanceof Boolean) result = (byte) ((Boolean) value ? 1 : 0);
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public Byte add(Byte left, Byte right) { return this.circularClamp(left + right); }
        public Byte subtract(Byte left, Byte right) { return this.circularClamp(left - right); }
        public Byte multiply(Byte left, Byte right) { return this.circularClamp(left * right); }
        public Byte divide(Byte left, Byte right) { return this.circularClamp(left / right); }
        public Byte mod(Byte left, Byte right) { return this.circularClamp(left % right); }
        private byte circularClamp(int value) {
            int range = max - min + 1;
            return (byte) (((((value - min) % range) + range) % range) + min);
        }
    }

    private static abstract class JavaShort extends JavaIntegralType<Short> {
        private JavaShort(String name, int byteSize, boolean signed, Short min, Short max,
                          ByteBufReader<Short> reader, ByteBufGetter<Short> getter,
                          ByteBufWriter<Short> writer, ByteBufSetter<Short> setter) {
            super(name, byteSize, signed, min, max, reader, getter, writer, setter, Short[]::new);
        }

        public Short unsafeCast(Object value) { return (Short) value; }
        public <U> int compare(Short left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Short staticCast(U value) {
            short result;
            if     (value instanceof Number ) result = ((Number) value).shortValue();
            else if(value instanceof Boolean) result = (short) ((Boolean) value ? 1 : 0);
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public Short add(Short left, Short right) { return this.circularClamp(left + right); }
        public Short subtract(Short left, Short right) { return this.circularClamp(left - right); }
        public Short multiply(Short left, Short right) { return this.circularClamp(left * right); }
        public Short divide(Short left, Short right) { return this.circularClamp(left / right); }
        public Short mod(Short left, Short right) { return this.circularClamp(left % right); }
        private short circularClamp(int value) {
            int range = max - min + 1;
            return (short) (((((value - min) % range) + range) % range) + min);
        }
    }

    private static abstract class JavaInteger extends JavaIntegralType<Integer> {
        private JavaInteger(String name, int byteSize, boolean signed, Integer min, Integer max,
                            ByteBufReader<Integer> reader, ByteBufGetter<Integer> getter,
                            ByteBufWriter<Integer> writer, ByteBufSetter<Integer> setter) {
            super(name, byteSize, signed, min, max, reader, getter, writer, setter, Integer[]::new);
        }

        public Integer unsafeCast(Object value) { return (Integer) value; }
        public <U> int compare(Integer left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Integer staticCast(U value) {
            int result;
            if     (value instanceof Number ) result = ((Number) value).intValue();
            else if(value instanceof Boolean) result = (Boolean) value ? 1 : 0;
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public Integer add(Integer left, Integer right) { return this.circularClamp(left + right); }
        public Integer subtract(Integer left, Integer right) { return this.circularClamp(left - right); }
        public Integer multiply(Integer left, Integer right) { return this.circularClamp(left * right); }
        public Integer divide(Integer left, Integer right) { return this.circularClamp(left / right); }
        public Integer mod(Integer left, Integer right) { return this.circularClamp(left % right); }
        private int circularClamp(int value) {
            long range = (long) max - min + 1;
            return (int) (((((value - min) % range) + range) % range) + min);
        }
    }

    private static abstract class JavaLong extends JavaIntegralType<Long> {
        private final BigInteger bigIntRange, bigIntMin;

        private JavaLong(String name, int byteSize, boolean signed, Long min, Long max,
                         ByteBufReader<Long> reader, ByteBufGetter<Long> getter,
                         ByteBufWriter<Long> writer, ByteBufSetter<Long> setter) {
            super(name, byteSize, signed, min, max, reader, getter, writer, setter, Long[]::new);
            BigInteger bigIntMax = BigInteger.valueOf(max);
            this.bigIntMin = BigInteger.valueOf(min);
            this.bigIntRange = bigIntMax.subtract(this.bigIntMin).add(BigInteger.ONE);
        }

        public Long unsafeCast(Object value) { return (Long) value; }
        public <U> int compare(Long left, U right) { return JavaLong.integralCompare(left, right); }
        public <U> Long staticCast(U value) {
            long result;
            if     (value instanceof Number ) result = ((Number) value).longValue();
            else if(value instanceof Boolean) result = (Boolean) value ? 1L : 0L;
            else throw new IllegalArgumentException();
            return this.circularClamp(result);
        }
        public Long add(Long left, Long right) { return this.circularClamp(left + right); }
        public Long subtract(Long left, Long right) { return this.circularClamp(left - right); }
        public Long multiply(Long left, Long right) { return this.circularClamp(left * right); }
        public Long divide(Long left, Long right) { return this.circularClamp(left / right); }
        public Long mod(Long left, Long right) { return this.circularClamp(left % right); }
        private long circularClamp(long value) {
            return BigInteger.valueOf(value)
                    .subtract(this.bigIntMin)
                    .mod(this.bigIntRange)
                    .add(this.bigIntRange)
                    .mod(this.bigIntRange)
                    .add(this.bigIntMin)
                    .longValue();
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

    private static abstract class JavaBigInteger extends JavaIntegralType<BigInteger> {
        private final BigInteger bigIntRange, bigIntMin;

        private JavaBigInteger(String name, int byteSize, boolean signed, BigInteger min, BigInteger max,
                               ByteBufReader<BigInteger> reader, ByteBufGetter<BigInteger> getter,
                               ByteBufWriter<BigInteger> writer, ByteBufSetter<BigInteger> setter) {
            super(name, byteSize, signed, min, max, reader, getter, writer, setter, BigInteger[]::new);
            this.bigIntMin = min;
            this.bigIntRange = max.subtract(min).add(BigInteger.ONE);
        }

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
        public BigInteger add(BigInteger left, BigInteger right) { return this.circularClamp(left.add(right)); }
        public BigInteger subtract(BigInteger left, BigInteger right) { return this.circularClamp(left.subtract(right)); }
        public BigInteger multiply(BigInteger left, BigInteger right) { return this.circularClamp(left.multiply(right)); }
        public BigInteger divide(BigInteger left, BigInteger right) { return this.circularClamp(left.divide(right)); }
        public BigInteger mod(BigInteger left, BigInteger right) { return this.circularClamp(left.mod(right)); }
        private BigInteger circularClamp(BigInteger value) {
            return value.subtract(this.bigIntMin)
                    .mod(this.bigIntRange)
                    .add(this.bigIntRange)
                    .mod(this.bigIntRange)
                    .add(this.bigIntMin);
        }

        private static BigInteger longToBigInteger(long value) {
            int upper = (int) (value >>> 32);
            int lower = (int) value;
            return (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                    add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
        }
    }

    private static abstract class JavaFloat extends DataType<Float> {
        private JavaFloat(String name, int byteSize, boolean signed, Float min, Float max,
                          ByteBufReader<Float> reader, ByteBufGetter<Float> getter,
                          ByteBufWriter<Float> writer, ByteBufSetter<Float> setter) {
            super(name, byteSize, false, true, signed, min, max,
                    reader, getter, writer, setter, Float[]::new);
        }

        public Float unsafeCast(Object value) { return (Float) value; }
        public <U> int compare(Float left, U right) { return JavaDouble.floatingPointCompare(left, right); }
        public <U> Float staticCast(U value) {
            if     (value instanceof Number ) return ((Number) value).floatValue();
            else if(value instanceof Boolean) return (Boolean) value ? 1f : 0f;
            else throw new IllegalArgumentException();
        }
        public Float add(Float left, Float right) { return left + right; }
        public Float subtract(Float left, Float right) { return left - right; }
        public Float multiply(Float left, Float right) { return left * right; }
        public Float divide(Float left, Float right) { return left / right; }
        public Float mod(Float left, Float right) { return left % right; }
        public Float floor(Float value) { return (float) Math.floor(value); }
    }

    private static abstract class JavaDouble extends DataType<Double> {
        private JavaDouble(String name, int byteSize, boolean signed, Double min, Double max,
                           ByteBufReader<Double> reader, ByteBufGetter<Double> getter,
                           ByteBufWriter<Double> writer, ByteBufSetter<Double> setter) {
            super(name, byteSize, false, true, signed, min, max,
                    reader, getter, writer, setter, Double[]::new);
        }

        public Double unsafeCast(Object value) { return (Double) value; }
        public <U> int compare(Double left, U right) { return JavaDouble.floatingPointCompare(left, right); }
        public <U> Double staticCast(U value) {
            if     (value instanceof Number) return ((Number) value).doubleValue();
            else if(value instanceof Boolean) return (Boolean) value ? 1d : 0d;
            else throw new IllegalArgumentException();
        }
        public Double add(Double left, Double right) { return left + right; }
        public Double subtract(Double left, Double right) { return left - right; }
        public Double multiply(Double left, Double right) { return left * right; }
        public Double divide(Double left, Double right) { return left / right; }
        public Double mod(Double left, Double right) { return left % right; }
        public Double floor(Double value) { return Math.floor(value); }

        private static int floatingPointCompare(double left, Object right) {
            if     (right instanceof BigInteger) return BigDecimal.valueOf(left).compareTo(new BigDecimal((BigInteger) right));
            else if(right instanceof Number    ) return Double.compare(left, ((Number) right).doubleValue());
            else if(right instanceof Boolean   ) return Double.compare(left, (Boolean) right ? 1 : 0);
            else throw new IllegalArgumentException("cannot compare float type with " + right.getClass());
        }
    }

    private static abstract class JavaBoolean extends JavaIntegralType<Boolean> {
        private JavaBoolean(String name, int byteSize, boolean signed, Boolean min, Boolean max,
                            ByteBufReader<Boolean> reader, ByteBufGetter<Boolean> getter,
                            ByteBufWriter<Boolean> writer, ByteBufSetter<Boolean> setter) {
            super(name, byteSize, signed, min, max, reader, getter, writer, setter, Boolean[]::new);
        }

        public Boolean unsafeCast(Object value) { return (Boolean) value; }
        public <U> int compare(Boolean left, U right) { return JavaLong.integralCompare(left ? 1 : 0, right); }
        public <U> Boolean staticCast(U value) {
            if     (value instanceof Number) return ((Number) value).intValue() != 0;
            else if(value instanceof Boolean) return (Boolean) value;
            else throw new IllegalArgumentException();
        }
        // Based on C++'s behaviour: "std::cout << (bool) (true op false) << std::endl"
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
    }

    private static abstract class JavaIntegralType<T> extends DataType<T> {
        private JavaIntegralType(String name, int byteSize, boolean signed, T min, T max,
                                 ByteBufReader<T> reader, ByteBufGetter<T> getter,
                                 ByteBufWriter<T> writer, ByteBufSetter<T> setter,
                                 Function<Integer, T[]> arrayMaker) {
            super(name, byteSize, true, false, signed, min, max,
                    reader, getter, writer, setter, arrayMaker);
        }
        public final T floor(T value) { return value; }
    }

    @FunctionalInterface private interface ByteBufReader<T> { T read(ByteBuf buf); }
    @FunctionalInterface private interface ByteBufGetter<T> { T get(ByteBuf buf, int index); }
    @FunctionalInterface private interface ByteBufWriter<T> { void write(ByteBuf buf, T value); }
    @FunctionalInterface private interface ByteBufSetter<T> { void set(ByteBuf buf, int index, T value); }
}
