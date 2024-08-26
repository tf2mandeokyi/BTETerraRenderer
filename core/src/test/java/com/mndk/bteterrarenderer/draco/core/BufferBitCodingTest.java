/*
 * Copyright (C) 2024 The Draco Authors (for providing the original C++ code)
 * Copyright (C) 2024 m4ndeokyi (for translating the code into Java)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mndk.bteterrarenderer.draco.core;

import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import org.junit.Assert;
import org.junit.Test;

public class BufferBitCodingTest {

    @Test
    public void testBitCodersByteAligned() {
        int bufferSize = 32;
        RawPointer pointer = RawPointer.newArray(bufferSize);
        EncoderBuffer.BitEncoder encoder = new EncoderBuffer.BitEncoder(pointer);
        byte[] data = {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10};
        int bytesToEncode = data.length;

        for (int i = 0; i < bytesToEncode; ++i) {
            encoder.putBits(UInt.of(data[i]), 8);
            Assert.assertEquals((i + 1) * 8, encoder.bits());
        }

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(pointer, bytesToEncode);
        for (int i = 0; i < data.length; i++) {
            byte datum = data[i];
            Pointer<UInt> x = Pointer.newUInt();
            StatusAssert.assertOk(decoder.getBits(8, x));
            Assert.assertEquals("Failed to read byte #" + i + ": " + datum, datum, x.get().byteValue());
        }

        Assert.assertEquals(bytesToEncode * 8, decoder.bitsDecoded());
    }

    @Test
    public void testBitCodersNonByte() {
        int bufferSize = 32;
        RawPointer pointer = RawPointer.newArray(bufferSize);
        EncoderBuffer.BitEncoder encoder = new EncoderBuffer.BitEncoder(pointer);
        byte[] data = {0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10};
        int bitsToEncode = 51;
        int bytesToEncode = (bitsToEncode / 8) + 1;

        for (int i = 0; i < bytesToEncode; ++i) {
            int numBits = (encoder.bits() + 8 <= bitsToEncode) ? 8 : (int) (bitsToEncode - encoder.bits());
            encoder.putBits(UInt.of(data[i]), numBits);
        }

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(pointer, bytesToEncode);
        long bitsToDecode = encoder.bits();
        for(int i = 0; i < bytesToEncode; i++) {
            byte datum = data[i];
            int numBits = (int) Math.min(bitsToDecode, 8);
            Pointer<UInt> x = Pointer.newUInt();
            StatusAssert.assertOk(decoder.getBits(numBits, x));
            int bitsToShift = 8 - numBits;
            byte testByte = (byte) (((datum << bitsToShift) & 0xff) >> bitsToShift);
            Assert.assertEquals(testByte, x.get().byteValue());
            bitsToDecode -= 8;
        }

        Assert.assertEquals(bitsToEncode, decoder.bitsDecoded());
    }

    @Test
    public void testSingleBits() {
        int data = 0xaaaa;
        RawPointer pointer = Pointer.newUInt(data).asRaw();

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(pointer, 4);

        for (int i = 0; i < 16; ++i) {
            Pointer<UInt> x = Pointer.newUInt();
            StatusAssert.assertOk(decoder.getBits(1, x));
            Assert.assertEquals((i % 2), x.get().intValue());
        }

        Assert.assertEquals(16, decoder.bitsDecoded());
    }

    @Test
    public void testMultipleBits() {
        byte[] data = { 0x76, 0x54, 0x32, 0x10, 0x76, 0x54, 0x32, 0x10 };
        RawPointer pointer = Pointer.wrap(data).asRaw();

        DecoderBuffer.BitDecoder decoder = new DecoderBuffer.BitDecoder();
        decoder.reset(pointer, data.length);

        for (int i = 0; i < 2; ++i) {
            Pointer<UInt> x = Pointer.newUInt();
            StatusAssert.assertOk(decoder.getBits(16, x));
            Assert.assertEquals(0x5476, x.get().intValue());
            Assert.assertEquals(16 + (i * 32), decoder.bitsDecoded());

            StatusAssert.assertOk(decoder.getBits(16, x));
            Assert.assertNotNull(x);
            Assert.assertEquals(0x1032, x.get().intValue());
            Assert.assertEquals(32 + (i * 32), decoder.bitsDecoded());
        }
    }

}
