package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface UByteArray extends BigArray<UByte> {
    static UByteArray create(long size) { return new OwnedUByteArray(size); }
    static UByteArray create(byte[] value) { return new OwnedUByteArray(value); }
    static UByteArray create(UByteArray other) { return new OwnedUByteArray(other); }
    static UByteArray create(String value, Charset charset) { return new OwnedUByteArray(value, charset); }

    void set(long index, byte value);

    String decode(long offset, Charset charset);
    default String decode(Charset charset) {
        return decode(0, charset);
    }
    default String decode() {
        return decode(StandardCharsets.UTF_8);
    }

    @Override
    UByteArray withOffset(long offset);

    UShort getUInt16(long index, Endian endian);
    UInt getUInt24(long index, Endian endian);
    UInt getUInt32(long index, Endian endian);
    ULong getUInt64(long index, Endian endian);
    void setUInt16(long index, UShort val, Endian endian);
    void setUInt24(long index, UInt val, Endian endian);
    void setUInt32(long index, UInt val, Endian endian);
    void setUInt64(long index, ULong val, Endian endian);
}
