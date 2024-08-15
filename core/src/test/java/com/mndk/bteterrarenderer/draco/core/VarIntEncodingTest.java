package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.PointerAssert;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import org.junit.Assert;
import org.junit.Test;

public class VarIntEncodingTest {

    private <T> void testVarint(Pointer<T> number, byte[] expected) {
        EncoderBuffer buffer = new EncoderBuffer();
        StatusAssert.assertOk(BitUtils.encodeVarint(number.getType().asNumber(), number.get(), buffer));
        PointerAssert.dataEquals(expected, buffer);

        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(buffer.getData(), buffer.size());
        Pointer<T> decodedRef = number.getType().newOwned();
        decoderBuffer.decodeVarint(decodedRef);
        Assert.assertEquals(number.get(), decodedRef.get());
    }

    @Test
    public void givenNumber_whenEncoded_thenCheck() {
        testVarint(Pointer.newUShort((short) 300), new byte[] { (byte) 0xac, (byte) 0x02 });
        testVarint(Pointer.newUInt(300), new byte[] { (byte) 0xac, (byte) 0x02 });
        testVarint(Pointer.newULong(300), new byte[] { (byte) 0xac, (byte) 0x02 });
        testVarint(Pointer.newShort((short) 300), new byte[] { (byte) 0xd8, (byte) 0x04 });
        testVarint(Pointer.newShort((short) -300), new byte[] { (byte) 0xd7, (byte) 0x04 });
        testVarint(Pointer.newInt(300), new byte[] { (byte) 0xd8, (byte) 0x04 });
        testVarint(Pointer.newInt(-300), new byte[] { (byte) 0xd7, (byte) 0x04 });
        testVarint(Pointer.newLong(300), new byte[] { (byte) 0xd8, (byte) 0x04 });
        testVarint(Pointer.newLong(-300), new byte[] { (byte) 0xd7, (byte) 0x04 });
    }

}
