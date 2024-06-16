package com.mndk.bteterrarenderer.draco.core;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class DataTypeTest {

    @Test
    public void givenIntegralDataTypes_testLiteralValueStaticCast() {
        Assert.assertEquals((long) DataType.INT8.staticCast(3), 3);
        Assert.assertEquals((long) DataType.INT8.staticCast(-128), -128);
        Assert.assertEquals((long) DataType.INT8.staticCast(254), -2);
        Assert.assertEquals((long) DataType.INT8.staticCast(-129), 127);
        Assert.assertEquals((long) DataType.INT8.staticCast(3.5), 3);

        Assert.assertEquals((long) DataType.UINT8.staticCast(6), 6);
        Assert.assertEquals((long) DataType.UINT8.staticCast(256), 0);
        Assert.assertEquals((long) DataType.UINT8.staticCast(257), 1);
        Assert.assertEquals((long) DataType.UINT8.staticCast(0), 0);
        Assert.assertEquals((long) DataType.UINT8.staticCast(-1), 255);

        Assert.assertEquals((long) DataType.INT16.staticCast(8), 8);
        Assert.assertEquals((long) DataType.INT16.staticCast(32768), -32768);
        Assert.assertEquals((long) DataType.INT16.staticCast(32769), -32767);
        Assert.assertEquals((long) DataType.INT16.staticCast(-32768), -32768);
        Assert.assertEquals((long) DataType.INT16.staticCast(-32769), 32767);

        Assert.assertEquals((long) DataType.UINT16.staticCast(5), 5);
        Assert.assertEquals((long) DataType.UINT16.staticCast(65536), 0);
        Assert.assertEquals((long) DataType.UINT16.staticCast(65537), 1);
        Assert.assertEquals((long) DataType.UINT16.staticCast(0), 0);
        Assert.assertEquals((long) DataType.UINT16.staticCast(-1), 65535);

        Assert.assertEquals((long) DataType.INT32.staticCast(420), 420);
        Assert.assertEquals((long) DataType.INT32.staticCast(2147483648L), -2147483648);
        Assert.assertEquals((long) DataType.INT32.staticCast(2147483649L), -2147483647);
        Assert.assertEquals((long) DataType.INT32.staticCast(-2147483648), -2147483648);
        Assert.assertEquals((long) DataType.INT32.staticCast(-2147483649L), 2147483647);

        Assert.assertEquals((long) DataType.UINT32.staticCast(69), 69);
        Assert.assertEquals((long) DataType.UINT32.staticCast(4294967296L), 0);
        Assert.assertEquals((long) DataType.UINT32.staticCast(4294967297L), 1);
        Assert.assertEquals((long) DataType.UINT32.staticCast(0), 0);
        Assert.assertEquals((long) DataType.UINT32.staticCast(-1), 4294967295L);

        Assert.assertEquals((long) DataType.INT64.staticCast(32), 32);
        Assert.assertEquals((long) DataType.INT64.staticCast(9223372036854775807L), 9223372036854775807L);
        Assert.assertEquals((long) DataType.INT64.staticCast(new BigInteger("9223372036854775808")), -9223372036854775808L);
        Assert.assertEquals((long) DataType.INT64.staticCast(-9223372036854775808L), -9223372036854775808L);
        Assert.assertEquals((long) DataType.INT64.staticCast(new BigInteger("-9223372036854775809")), 9223372036854775807L);

        Assert.assertEquals(DataType.UINT64.staticCast(42), BigInteger.valueOf(42));
        Assert.assertEquals(DataType.UINT64.staticCast(new BigInteger("18446744073709551616")), BigInteger.ZERO);
        Assert.assertEquals(DataType.UINT64.staticCast(new BigInteger("18446744073709551617")), BigInteger.ONE);
        Assert.assertEquals(DataType.UINT64.staticCast(-1), new BigInteger("18446744073709551615"));
    }

    // Do test for add, subtract, multiply, divide, mod, negate, abs, floor, and, or, xor, not, shiftLeft,
    // shiftArithRight, shiftLogicRight, and equals

    @Test
    public void givenArithmeticOperationAdd_doBoundaryTestForAllIntegralTypes() {
        Assert.assertEquals((byte) DataType.INT8.add((byte) 3, (byte) 5), 8);
        Assert.assertEquals((byte) DataType.INT8.add((byte) 127, (byte) 1), -128);
        Assert.assertEquals((byte) DataType.INT8.add((byte) -128, (byte) -1), 127);
        Assert.assertEquals((byte) DataType.INT8.add((byte) -128, (byte) 0), -128);

        Assert.assertEquals((short) DataType.UINT8.add((short) 3, (short) 5), 8);
        Assert.assertEquals((short) DataType.UINT8.add((short) 255, (short) 1), 0);
        Assert.assertEquals((short) DataType.UINT8.add((short) 255, (short) 2), 1);

        Assert.assertEquals((short) DataType.INT16.add((short) 3, (short) 5), 8);
        Assert.assertEquals((short) DataType.INT16.add((short) 32767, (short) 1), -32768);
        Assert.assertEquals((short) DataType.INT16.add((short) -32768, (short) -1), 32767);
        Assert.assertEquals((short) DataType.INT16.add((short) -32768, (short) 0), -32768);

        Assert.assertEquals((int) DataType.UINT16.add(3, 5), 8);
        Assert.assertEquals((int) DataType.UINT16.add(65535, 1), 0);
        Assert.assertEquals((int) DataType.UINT16.add(65535, 2), 1);

        Assert.assertEquals((int) DataType.INT32.add(3, 5), 8);
        Assert.assertEquals((int) DataType.INT32.add(2147483647, 1), -2147483648);
        Assert.assertEquals((int) DataType.INT32.add(-2147483648, -1), 2147483647);
        Assert.assertEquals((int) DataType.INT32.add(-2147483648, 0), -2147483648);

        Assert.assertEquals((long) DataType.UINT32.add(3L, 5L), 8);
        Assert.assertEquals((long) DataType.UINT32.add(4294967295L, 1L), 0);
        Assert.assertEquals((long) DataType.UINT32.add(4294967295L, 2L), 1);

        Assert.assertEquals((long) DataType.INT64.add(3L, 5L), 8L);
        Assert.assertEquals((long) DataType.INT64.add(9223372036854775807L, 1L), -9223372036854775808L);
        Assert.assertEquals((long) DataType.INT64.add(-9223372036854775808L, -1L), 9223372036854775807L);
        Assert.assertEquals((long) DataType.INT64.add(-9223372036854775808L, 0L), -9223372036854775808L);

        Assert.assertEquals(DataType.UINT64.add(BigInteger.valueOf(3), BigInteger.valueOf(5)), BigInteger.valueOf(8));
        Assert.assertEquals(DataType.UINT64.add(new BigInteger("18446744073709551615"), BigInteger.ONE), BigInteger.ZERO);
        Assert.assertEquals(DataType.UINT64.add(new BigInteger("18446744073709551615"), new BigInteger("2")), BigInteger.ONE);
    }

    @Test
    public void givenArithmeticOperationSubtract_doBoundaryTestForAllIntegralTypes() {
        Assert.assertEquals((byte) DataType.INT8.subtract((byte) 3, (byte) 5), -2);
        Assert.assertEquals((byte) DataType.INT8.subtract((byte) -128, (byte) 1), 127);
        Assert.assertEquals((byte) DataType.INT8.subtract((byte) 127, (byte) -1), -128);
        Assert.assertEquals((byte) DataType.INT8.subtract((byte) -128, (byte) 0), -128);

        Assert.assertEquals((short) DataType.UINT8.subtract((short) 3, (short) 5), 254);
        Assert.assertEquals((short) DataType.UINT8.subtract((short) 0, (short) 1), 255);
        Assert.assertEquals((short) DataType.UINT8.subtract((short) 1, (short) 2), 255);

        Assert.assertEquals((short) DataType.INT16.subtract((short) 3, (short) 5), -2);
        Assert.assertEquals((short) DataType.INT16.subtract((short) -32768, (short) 1), 32767);
        Assert.assertEquals((short) DataType.INT16.subtract((short) 32767, (short) -1), -32768);
        Assert.assertEquals((short) DataType.INT16.subtract((short) -32768, (short) 0), -32768);

        Assert.assertEquals((int) DataType.UINT16.subtract(3, 5), 65534);
        Assert.assertEquals((int) DataType.UINT16.subtract(0, 1), 65535);
        Assert.assertEquals((int) DataType.UINT16.subtract(1, 2), 65535);

        Assert.assertEquals((int) DataType.INT32.subtract(3, 5), -2);
        Assert.assertEquals((int) DataType.INT32.subtract(-2147483648, 1), 2147483647);
        Assert.assertEquals((int) DataType.INT32.subtract(2147483647, -1), -2147483648);
        Assert.assertEquals((int) DataType.INT32.subtract(-2147483648, 0), -2147483648);

        Assert.assertEquals((long) DataType.UINT32.subtract(3L, 5L), 4294967294L);
        Assert.assertEquals((long) DataType.UINT32.subtract(0L, 1L), 4294967295L);
        Assert.assertEquals((long) DataType.UINT32.subtract(1L, 2L), 4294967295L);

        Assert.assertEquals((long) DataType.INT64.subtract(3L, 5L), -2L);
        Assert.assertEquals((long) DataType.INT64.subtract(-9223372036854775808L, 1L), 9223372036854775807L);
        Assert.assertEquals((long) DataType.INT64.subtract(9223372036854775807L, -1L), -9223372036854775808L);
        Assert.assertEquals((long) DataType.INT64.subtract(-9223372036854775808L, 0L), -9223372036854775808L);

        Assert.assertEquals(DataType.UINT64.subtract(BigInteger.valueOf(3), BigInteger.valueOf(5)), new BigInteger("18446744073709551614"));
        Assert.assertEquals(DataType.UINT64.subtract(BigInteger.ZERO, BigInteger.ONE), new BigInteger("18446744073709551615"));
        Assert.assertEquals(DataType.UINT64.subtract(BigInteger.ONE, new BigInteger("2")), new BigInteger("18446744073709551615"));
    }
}
