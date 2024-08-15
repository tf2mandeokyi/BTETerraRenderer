package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface BigUByteArray extends BigArray<UByte> {

    BigUByteArray EMPTY = BigUByteArray.create(0);

    static BigUByteArray create(long size) { return new OwnedBigUByteArray(size); }
    static BigUByteArray create(byte[] value) { return new OwnedBigUByteArray(value); }
    static BigUByteArray create(BigUByteArray other) { return new OwnedBigUByteArray(other); }
    static BigUByteArray create(String value, Charset charset) { return new OwnedBigUByteArray(value, charset); }
    static BigUByteArray create(InputStream stream) throws IOException { return new OwnedBigUByteArray(stream); }
    static BigUByteArray create(ByteBuf buf) { return new OwnedBigUByteArray(buf); }
    static BigUByteArray create(ByteBuffer buffer) { return new OwnedBigUByteArray(buffer); }
    static BigUByteArray create(RawPointer src, long byteLength) {
        return new OwnedBigUByteArray(src, byteLength);
    }

    void set(long index, byte value);

    String decode(long offset, int length, Charset charset);
    default String decode() { return decode(StandardCharsets.UTF_8); }
    default String decode(Charset charset) {
        return decode(0, (int) Math.min(size(), MAX_INNER_SIZE), charset);
    }

    default <T> Pointer<T> getPointer(DataType<T> type) { return this.getRawPointer().toType(type); }
    default <T> Pointer<T> getPointer(DataType<T> type, long byteOffset) {
        return this.getRawPointer(byteOffset).toType(type);
    }
}
