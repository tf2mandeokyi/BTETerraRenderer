package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.ObjectType;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;

@RequiredArgsConstructor
public class StringType extends ObjectType<String> {
    private final int byteLength;
    private final Charset charset;

    // IO operations
    @Override public long size() { return byteLength; }
    @Override public String read(UByteArray array, long index, Endian endian) {
        UByteArray result = UByteArray.create(byteLength);
        array.copyTo(index, result, 0, byteLength);
        return result.decode(charset);
    }
    @Override public void write(UByteArray array, long index, String value, Endian endian) {
        UByteArray encoded = UByteArray.create(value, charset);
        array.copyTo(0, encoded, index, byteLength);
    }

    // General conversions
    @Override public String parse(String value) { return value; }
}
