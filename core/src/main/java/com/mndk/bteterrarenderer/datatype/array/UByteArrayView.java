package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;

import java.nio.charset.Charset;

class UByteArrayView extends BigArrayView<UByte, UByteArray> implements UByteArray {

    UByteArrayView(UByteArray array, long offset) {
        super(array, offset);
    }

    @Override
    public void set(long index, byte value) {
        array.set(index + offset, value);
    }

    @Override
    public UByteArray withOffset(long offset) {
        return new UByteArrayView(array, this.offset + offset);
    }

    @Override public String decode(long offset, Charset charset) { return array.decode(offset, charset); }
    @Override public UShort getUInt16(long i, Endian e) { return array.getUInt16(i + offset, e); }
    @Override public UInt getUInt24(long i, Endian e) { return array.getUInt24(i + offset, e); }
    @Override public UInt getUInt32(long i, Endian e) { return array.getUInt32(i + offset, e); }
    @Override public ULong getUInt64(long i, Endian e) { return array.getUInt64(i + offset, e); }
    @Override public void setUInt16(long i, UShort val, Endian e) { array.setUInt16(i + offset, val, e); }
    @Override public void setUInt24(long i, UInt val, Endian e) { array.setUInt24(i + offset, val, e); }
    @Override public void setUInt32(long i, UInt val, Endian e) { array.setUInt32(i + offset, val, e); }
    @Override public void setUInt64(long i, ULong val, Endian e) { array.setUInt64(i + offset, val, e); }
}
