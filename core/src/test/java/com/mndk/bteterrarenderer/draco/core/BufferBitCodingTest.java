package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.UByteArray;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.UShort;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class BufferBitCodingTest {

    @Test
    public void testBitCodersByteAligned() {
        int bufferSize = 32;
        DataBuffer buffer = new DataBuffer(bufferSize);
        EncoderBuffer.BitEncoder encoder = new EncoderBuffer.BitEncoder(buffer);
        byte[] data = {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10};
        int bytesToEncode = data.length;

        for (int i = 0; i < bytesToEncode; ++i) {
            encoder.putBits(UInt.of(data[i]), 8);
            Assert.assertEquals((i + 1) * 8, encoder.bits());
        }

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(buffer, bytesToEncode);
        for (int i = 0; i < data.length; i++) {
            byte datum = data[i];
            AtomicReference<UInt> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(UInt.of(8), x::set));
            Assert.assertEquals("Failed to read byte #" + i + ": " + datum, datum, x.get().byteValue());
        }

        Assert.assertEquals(bytesToEncode * 8, decoder.bitsDecoded());
    }

    @Test
    public void testBitCodersNonByte() {
        int bufferSize = 32;
        DataBuffer buffer = new DataBuffer(bufferSize);
        EncoderBuffer.BitEncoder encoder = new EncoderBuffer.BitEncoder(buffer);
        byte[] data = {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10};
        int bitsToEncode = 51;
        int bytesToEncode = (bitsToEncode / 8) + 1;

        for (int i = 0; i < bytesToEncode; ++i) {
            int numBits = (encoder.bits() + 8 <= bitsToEncode) ? 8 : (int) (bitsToEncode - encoder.bits());
            encoder.putBits(UInt.of(data[i]), numBits);
        }

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(buffer, bytesToEncode);
        long bitsToDecode = encoder.bits();
        for(int i = 0; i < bytesToEncode; i++) {
            byte datum = data[i];
            int numBits = (int) Math.min(bitsToDecode, 8);
            AtomicReference<UInt> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(UInt.of(numBits), x::set));
            int bitsToShift = 8 - numBits;
            byte testByte = (byte) (((datum << bitsToShift) & 0xff) >> bitsToShift);
            Assert.assertEquals(testByte, x.get().byteValue());
            bitsToDecode -= 8;
        }

        Assert.assertEquals(bitsToEncode, decoder.bitsDecoded());
    }

    @Test
    public void testSingleBits() {
        long shortSize = DataType.uint16().size();
        DataBuffer data = new DataBuffer(shortSize);
        data.write(DataType.uint16(), 0, UShort.of(0xaaaa));

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(data, shortSize);

        for (int i = 0; i < 16; ++i) {
            AtomicReference<UInt> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(UInt.of(1), x::set));
            Assert.assertEquals((i % 2), x.get().intValue());
        }

        Assert.assertEquals(16, decoder.bitsDecoded());
    }

    @Test
    public void testMultipleBits() {
        DataBuffer data = new DataBuffer(8);
        UByteArray bytes = UByteArray.create(new byte[] {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10});
        data.write(DataType.bytes(bytes.size()), 0, bytes);

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(data, bytes.size());

        for (int i = 0; i < 2; ++i) {
            AtomicReference<UInt> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(UInt.of(16), x::set));
            Assert.assertEquals(0x5476, x.get().intValue());
            Assert.assertEquals(16 + (i * 32), decoder.bitsDecoded());

            StatusAssert.assertOk(decoder.getBits(UInt.of(16), x::set));
            Assert.assertNotNull(x);
            Assert.assertEquals(0x1032, x.get().intValue());
            Assert.assertEquals(32 + (i * 32), decoder.bitsDecoded());
        }
    }

}
