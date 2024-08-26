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
