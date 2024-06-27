package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class VarIntEncodingTest {

    private ByteBuf copyBuffer(ByteBuf buffer) {
        buffer.markReaderIndex().markWriterIndex();
        ByteBuf result = Unpooled.copiedBuffer(buffer);
        buffer.resetReaderIndex().resetWriterIndex();
        return result;
    }

    private <T> void testVarint(DataNumberType<T, ?> dataType, T number, byte[] expected) {
        EncoderBuffer buffer = new EncoderBuffer();
        StatusAssert.assertOk(BitUtils.encodeVarint(dataType, number, buffer));
        DataBuffer dataBuffer = buffer.getData();
        for(int i = 0; i < expected.length; ++i) {
            Assert.assertEquals("Byte at index " + i, UByte.of(expected[i]), dataBuffer.get(i));
        }

        DecoderBuffer decoderBuffer = new DecoderBuffer();
        decoderBuffer.init(dataBuffer);
        AtomicReference<T> decodedRef = new AtomicReference<>();
        BitUtils.decodeVarint(dataType, decodedRef, decoderBuffer);
        Assert.assertEquals(number, decodedRef.get());
    }

    @Test
    public void givenNumber_whenEncoded_thenCheck() {
        testVarint(DataType.uint16(), UShort.of(300), new byte[] { (byte) 0xac, (byte) 0x02 });
        testVarint(DataType.uint32(), UInt.of(300), new byte[] { (byte) 0xac, (byte) 0x02 });
        testVarint(DataType.uint64(), ULong.of(300), new byte[] { (byte) 0xac, (byte) 0x02 });
        testVarint(DataType.int16(), (short) 300, new byte[] { (byte) 0xd8, (byte) 0x04 });
        testVarint(DataType.int16(), (short) -300, new byte[] { (byte) 0xd7, (byte) 0x04 });
        testVarint(DataType.int32(), 300, new byte[] { (byte) 0xd8, (byte) 0x04 });
        testVarint(DataType.int32(), -300, new byte[] { (byte) 0xd7, (byte) 0x04 });
        testVarint(DataType.int64(), 300L, new byte[] { (byte) 0xd8, (byte) 0x04 });
        testVarint(DataType.int64(), -300L, new byte[] { (byte) 0xd7, (byte) 0x04 });
    }

}
