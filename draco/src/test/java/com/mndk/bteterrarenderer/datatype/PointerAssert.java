package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import com.mndk.bteterrarenderer.draco.core.EncoderBuffer;
import org.junit.Assert;

public class PointerAssert {

    public static void dataEquals(byte[] expected, EncoderBuffer actual) {
        Assert.assertEquals(expected.length, actual.size());
        rawDataEquals(Pointer.wrap(expected).asRaw(), actual.getData(), expected.length);
    }

    public static void rawDataEquals(RawPointer expected, RawPointer actual, long size) {
        for (long i = 0; i < size; ++i) {
            Assert.assertEquals("Byte at index " + i, expected.getRawByte(i), actual.getRawByte(i));
        }
    }

}
