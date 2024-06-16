package com.mndk.bteterrarenderer.draco.core;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class BufferBitCodingTest {

    @Test
    public void testBitCodersByteAligned() {
        int bufferSize = 32;
        DataBuffer buffer = new DataBuffer(bufferSize);
        EncoderBuffer.BitEncoder encoder = new EncoderBuffer.BitEncoder(buffer, 0);
        byte[] data = {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10};
        int bytesToEncode = data.length;

        for (int i = 0; i < bytesToEncode; ++i) {
            encoder.putBits(data[i], 8);
            Assert.assertEquals((i + 1) * 8, encoder.bits());
        }

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(buffer, 0, bytesToEncode);
        for(byte datum : data) {
            AtomicReference<Long> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(8, x::set));
            Assert.assertEquals(datum, x.get().byteValue());
        }

        Assert.assertEquals(bytesToEncode * 8, decoder.bitsDecoded());
    }

    @Test
    public void testBitCodersNonByte() {
        int bufferSize = 32;
        DataBuffer buffer = new DataBuffer(bufferSize);
        EncoderBuffer.BitEncoder encoder = new EncoderBuffer.BitEncoder(buffer, 0);
        byte[] data = {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10};
        int bitsToEncode = 51;
        int bytesToEncode = (bitsToEncode / 8) + 1;

        for (int i = 0; i < bytesToEncode; ++i) {
            int num_bits = (encoder.bits() + 8 <= bitsToEncode) ? 8 : bitsToEncode - encoder.bits();
            encoder.putBits(data[i], num_bits);
        }

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(buffer, 0, bytesToEncode);
        int bitsToDecode = encoder.bits();
        for (byte datum : data) {
            int numBits = Math.min(bitsToDecode, 8);
            AtomicReference<Long> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(numBits, x::set));
            int bitsToShift = 8 - numBits;
            byte testByte = (byte) (((datum << bitsToShift) & 0xff) >> bitsToShift);
            Assert.assertEquals(testByte, x.get().byteValue());
            bitsToDecode -= 8;
        }

        Assert.assertEquals(bitsToEncode, decoder.bitsDecoded());
    }

    @Test
    public void testSingleBits() {
        DataBuffer data = new DataBuffer(DataType.UINT16.size());
        DataType.UINT16.setBuf(data, 0, 0xaaaa);

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(data, 0, DataType.UINT16.size());

        for (int i = 0; i < 16; ++i) {
            AtomicReference<Long> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(1, x::set));
            Assert.assertEquals((i % 2), x.get().intValue());
        }

        Assert.assertEquals(16, decoder.bitsDecoded());
    }

    @Test
    public void testMultipleBits() {
        DataBuffer data = new DataBuffer(8);
        byte[] bytes = {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10};
        DataType.bytes(bytes.length).setBuf(data, 0, bytes);

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(data, 0, bytes.length);

        for (int i = 0; i < 2; ++i) {
            AtomicReference<Long> x = new AtomicReference<>();
            StatusAssert.assertOk(decoder.getBits(16, x::set));
            Assert.assertEquals(0x5476, x.get().intValue());
            Assert.assertEquals(16 + (i * 32), decoder.bitsDecoded());

            StatusAssert.assertOk(decoder.getBits(16, x::set));
            Assert.assertNotNull(x);
            Assert.assertEquals(0x1032, x.get().intValue());
            Assert.assertEquals(32 + (i * 32), decoder.bitsDecoded());
        }
    }

}
