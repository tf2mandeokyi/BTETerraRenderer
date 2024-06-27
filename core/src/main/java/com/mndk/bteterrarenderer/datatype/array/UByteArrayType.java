package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.ObjectType;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class UByteArrayType extends ObjectType<UByteArray> {
    private final long size;

    // IO operations
    @Override public long size() { return size; }
    @Override public UByteArray read(UByteArray array, long index, Endian endian) {
        OwnedUByteArray result = new OwnedUByteArray(size);
        array.copyTo(index, result, 0, size);
        return result;
    }
    @Override public void write(UByteArray array, long index, UByteArray value, Endian endian) {
        value.copyTo(0, array, index, size);
    }

    // General conversions
    @Override public UByteArray parse(String value) { return new OwnedUByteArray(value, StandardCharsets.UTF_8); }
}
