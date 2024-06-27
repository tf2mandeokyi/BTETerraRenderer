package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import org.junit.Assert;
import org.junit.Test;

public class NumberDataTypeTest {

    // Because addition, subtraction, multiplication, left shift, bitwise operations work
    // the same way for both signed and unsigned numbers, we only need to test
    // for string representation, division(div), modulus(mod), and right shift(shr).
    // TODO: Add tests for other operations

    @Test
    public void givenNumber_testToString() {
        // byte
        Assert.assertEquals(DataType.int8().toString((byte) 10), "10");
        Assert.assertEquals(DataType.int8().toString((byte) -6), "-6");
        Assert.assertEquals(DataType.uint8().toString(UByte.of(10)), "10");
        Assert.assertEquals(DataType.uint8().toString(UByte.of(-6)), "250");

        // short
        Assert.assertEquals(DataType.int16().toString((short) 10), "10");
        Assert.assertEquals(DataType.int16().toString((short) -6), "-6");
        Assert.assertEquals(DataType.uint16().toString(UShort.of(10)), "10");
        Assert.assertEquals(DataType.uint16().toString(UShort.of(-6)), "65530");

        // int
        Assert.assertEquals(DataType.int32().toString(10), "10");
        Assert.assertEquals(DataType.int32().toString(-6), "-6");
        Assert.assertEquals(DataType.uint32().toString(UInt.of(10)), "10");
        Assert.assertEquals(DataType.uint32().toString(UInt.of(-6)), "4294967290");

        // long
        Assert.assertEquals(DataType.int64().toString(10L), "10");
        Assert.assertEquals(DataType.int64().toString(-6L), "-6");
        Assert.assertEquals(DataType.uint64().toString(ULong.of(10L)), "10");
        Assert.assertEquals(DataType.uint64().toString(ULong.of(-6L)), "18446744073709551610");
    }

    @Test
    public void givenTwoNumbers_whenDivide_thenCorrect() {
        // byte
        Assert.assertEquals(DataType.int8().div(new Byte((byte) 10), new Byte((byte) 2)).byteValue(), (byte) 5);
        Assert.assertEquals(DataType.int8().div(new Byte((byte) -6), new Byte((byte) 2)).byteValue(), (byte) -3);
        Assert.assertEquals(DataType.uint8().div(UByte.of(10), UByte.of(2)).byteValue(), (byte) 5);
        Assert.assertEquals(DataType.uint8().div(UByte.of(-6), UByte.of(2)).byteValue(), (byte) 125);

        // short
        Assert.assertEquals(DataType.int16().div(new Short((short) 10), new Short((short) 2)).shortValue(), (short) 5);
        Assert.assertEquals(DataType.int16().div(new Short((short) -6), new Short((short) 2)).shortValue(), (short) -3);
        Assert.assertEquals(DataType.uint16().div(UShort.of(10), UShort.of(2)).shortValue(), (short) 5);
        Assert.assertEquals(DataType.uint16().div(UShort.of(-6), UShort.of(2)).shortValue(), (short) 32765);

        // int
        Assert.assertEquals(DataType.int32().div(new Integer(10), new Integer(2)).intValue(), 5);
        Assert.assertEquals(DataType.int32().div(new Integer(-6), new Integer(2)).intValue(), -3);
        Assert.assertEquals(DataType.uint32().div(UInt.of(10), UInt.of(2)).intValue(), 5);
        Assert.assertEquals(DataType.uint32().div(UInt.of(-6), UInt.of(2)).intValue(), 2147483645);

        // long
        Assert.assertEquals(DataType.int64().div(new Long(10L), new Long(2L)).longValue(), 5L);
        Assert.assertEquals(DataType.int64().div(new Long(-6L), new Long(2L)).longValue(), -3L);
        Assert.assertEquals(DataType.uint64().div(ULong.of(10L), ULong.of(2L)).longValue(), 5L);
        Assert.assertEquals(DataType.uint64().div(ULong.of(-6L), ULong.of(2L)).longValue(), 9223372036854775805L);
    }


}
