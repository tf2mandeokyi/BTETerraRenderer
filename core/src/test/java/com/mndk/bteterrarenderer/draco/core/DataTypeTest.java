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

}
