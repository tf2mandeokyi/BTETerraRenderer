package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import org.junit.Assert;
import org.junit.Test;

public class NumberDataTypeTest {

    @Test
    public void givenNumber_testToString() {
        // byte
        Assert.assertEquals("10", DataType.int8().toString((byte) 10));
        Assert.assertEquals("-6", DataType.int8().toString((byte) -6));
        Assert.assertEquals("10", DataType.uint8().toString(UByte.of(10)));
        Assert.assertEquals("250", DataType.uint8().toString(UByte.of(-6)));

        // short
        Assert.assertEquals("10", DataType.int16().toString((short) 10));
        Assert.assertEquals("-6", DataType.int16().toString((short) -6));
        Assert.assertEquals("10", DataType.uint16().toString(UShort.of(10)));
        Assert.assertEquals("65530", DataType.uint16().toString(UShort.of(-6)));

        // int
        Assert.assertEquals("10", DataType.int32().toString(10));
        Assert.assertEquals("-6", DataType.int32().toString(-6));
        Assert.assertEquals("10", DataType.uint32().toString(UInt.of(10)));
        Assert.assertEquals("4294967290", DataType.uint32().toString(UInt.of(-6)));

        // long
        Assert.assertEquals("10", DataType.int64().toString(10L));
        Assert.assertEquals("-6", DataType.int64().toString(-6L));
        Assert.assertEquals("10", DataType.uint64().toString(ULong.of(10L)));
        Assert.assertEquals("18446744073709551610", DataType.uint64().toString(ULong.of(-6L)));
    }

    @Test
    public void givenTwoNumbers_whenDivide_thenCorrect() {
        // byte
        Assert.assertEquals((byte) 5, DataType.int8().div(new Byte((byte) 10), new Byte((byte) 2)).byteValue());
        Assert.assertEquals((byte) -3, DataType.int8().div(new Byte((byte) -6), new Byte((byte) 2)).byteValue());
        Assert.assertEquals((byte) 5, DataType.uint8().div(UByte.of(10), UByte.of(2)).byteValue());
        Assert.assertEquals((byte) 125, DataType.uint8().div(UByte.of(-6), UByte.of(2)).byteValue());

        // short
        Assert.assertEquals((short) 5, DataType.int16().div(new Short((short) 10), new Short((short) 2)).shortValue());
        Assert.assertEquals((short) -3, DataType.int16().div(new Short((short) -6), new Short((short) 2)).shortValue());
        Assert.assertEquals((short) 5, DataType.uint16().div(UShort.of(10), UShort.of(2)).shortValue());
        Assert.assertEquals((short) 32765, DataType.uint16().div(UShort.of(-6), UShort.of(2)).shortValue());

        // int
        Assert.assertEquals(5, DataType.int32().div(new Integer(10), new Integer(2)).intValue());
        Assert.assertEquals(-3, DataType.int32().div(new Integer(-6), new Integer(2)).intValue());
        Assert.assertEquals(5, DataType.uint32().div(UInt.of(10), UInt.of(2)).intValue());
        Assert.assertEquals(2147483645, DataType.uint32().div(UInt.of(-6), UInt.of(2)).intValue());

        // long
        Assert.assertEquals(5L, DataType.int64().div(new Long(10L), new Long(2L)).longValue());
        Assert.assertEquals(-3L, DataType.int64().div(new Long(-6L), new Long(2L)).longValue());
        Assert.assertEquals(5L, DataType.uint64().div(ULong.of(10L), ULong.of(2L)).longValue());
        Assert.assertEquals(9223372036854775805L, DataType.uint64().div(ULong.of(-6L), ULong.of(2L)).longValue());
    }


}
