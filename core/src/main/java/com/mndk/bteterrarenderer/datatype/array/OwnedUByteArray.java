package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.number.UInt;
import com.mndk.bteterrarenderer.datatype.number.ULong;
import com.mndk.bteterrarenderer.datatype.number.UShort;

import java.nio.charset.Charset;
import java.util.Arrays;

class OwnedUByteArray extends OwnedBigArray<UByte, byte[]> implements UByteArray {

    OwnedUByteArray(long size) { super(size); }
    OwnedUByteArray(byte[] value) { super(value); }
    OwnedUByteArray(UByteArray other) { super(other); }
    OwnedUByteArray(String value, Charset charset) { super(value.getBytes(charset)); }

    private int getRaw(long index) { return this.get(index).intValue(); }
    public void set(long index, byte value) { this.set(index, UByte.of(value)); }

    @Override public UByteArray withOffset(long offset) { return new UByteArrayView(this, offset); }
    @Override protected DataType<UByte, byte[]> getElementType() { return DataType.uint8(); }
    @Override protected byte[][] createOuterArray(int outerLength) { return new byte[outerLength][]; }
    @Override protected boolean deepEquals(byte[][] a, byte[][] b) { return Arrays.deepEquals(a, b); }
    @Override protected int deepHashCode(byte[][] a) { return Arrays.deepHashCode(a); }

    public String decode(long offset, Charset charset) {
        byte[] result = new byte[(int) Math.min(this.size() - offset, MAX_INNER_SIZE)];
        for(int i = 0; i < result.length; ++i) {
            result[i] = this.get(offset + i).byteValue();
        }
        return new String(result, charset);
    }
    public String decode(Charset charset) {
        // Since strings can't have bytes longer than MAX_INNER_SIZE, we only treat the first inner array.
        byte[] innerArray = this.getFirstInnerArray();
        return new String(innerArray, charset);
    }

    public UShort getUInt16(long index, Endian endian) {
        return endian == Endian.BIG ? this.getUInt16BE(index) : this.getUInt16LE(index);
    }
    private UShort getUInt16BE(long index) {
        // To compute the values fast without allocating new objects,
        // we first calculate the result using primitive and then convert it to the desired type.
        int result = this.getRaw(index) << 8;
        result    |= this.getRaw(index + 1);
        return UShort.of((short) result);
    }
    private UShort getUInt16LE(long index) {
        int result = this.getRaw(index);
        result    |= this.getRaw(index + 1) << 8;
        return UShort.of((short) result);
    }

    public UInt getUInt24(long index, Endian endian) {
        return endian == Endian.BIG ? this.getUInt24BE(index) : this.getUInt24LE(index);
    }
    private UInt getUInt24BE(long index) {
        int result = this.getRaw(index) << 16;
        result    |= this.getRaw(index + 1) << 8;
        result    |= this.getRaw(index + 2);
        return UInt.of(result);
    }
    private UInt getUInt24LE(long index) {
        int result = this.getRaw(index);
        result    |= this.getRaw(index + 1) <<  8;
        result    |= this.getRaw(index + 2) << 16;
        return UInt.of(result);
    }

    public UInt getUInt32(long index, Endian endian) {
        return endian == Endian.BIG ? this.getUInt32BE(index) : this.getUInt32LE(index);
    }
    private UInt getUInt32BE(long index) {
        int result = this.getRaw(index) << 24;
        result    |= this.getRaw(index + 1) << 16;
        result    |= this.getRaw(index + 2) <<  8;
        result    |= this.getRaw(index + 3);
        return UInt.of(result);
    }
    private UInt getUInt32LE(long index) {
        int result = this.getRaw(index);
        result    |= this.getRaw(index + 1) <<  8;
        result    |= this.getRaw(index + 2) << 16;
        result    |= this.getRaw(index + 3) << 24;
        return UInt.of(result);
    }

    public ULong getUInt64(long index, Endian endian) {
        return endian == Endian.BIG ? this.getUInt64BE(index) : this.getUInt64LE(index);
    }
    private ULong getUInt64BE(long index) {
        long result = (long) this.getRaw(index) << 56;
        result     |= (long) this.getRaw(index + 1) << 48;
        result     |= (long) this.getRaw(index + 2) << 40;
        result     |= (long) this.getRaw(index + 3) << 32;
        result     |= (long) this.getRaw(index + 4) << 24;
        result     |= (long) this.getRaw(index + 5) << 16;
        result     |= (long) this.getRaw(index + 6) <<  8;
        result     |=        this.getRaw(index + 7);
        return ULong.of(result);
    }
    private ULong getUInt64LE(long index) {
        long result = this.getRaw(index);
        result     |= (long) this.getRaw(index + 1) <<  8;
        result     |= (long) this.getRaw(index + 2) << 16;
        result     |= (long) this.getRaw(index + 3) << 24;
        result     |= (long) this.getRaw(index + 4) << 32;
        result     |= (long) this.getRaw(index + 5) << 40;
        result     |= (long) this.getRaw(index + 6) << 48;
        result     |= (long) this.getRaw(index + 7) << 56;
        return ULong.of(result);
    }

    public void setUInt16(long index, UShort val, Endian endian) {
        if(endian == Endian.BIG) this.setUInt16BE(index, val);
        else this.setUInt16LE(index, val);
    }
    private void setUInt16BE(long index, UShort val) {
        // To compute the values fast without allocating new objects,
        // we first calculate the result using primitive and then convert it to the desired type.
        int value = val.intValue();
        this.set(index    , (byte) (value >>> 8));
        this.set(index + 1, (byte)  value);
    }
    private void setUInt16LE(long index, UShort val) {
        int value = val.intValue();
        this.set(index    , (byte)  value);
        this.set(index + 1, (byte) (value >>> 8));
    }

    public void setUInt24(long index, UInt val, Endian endian) {
        if(endian == Endian.BIG) this.setUInt24BE(index, val);
        else this.setUInt24LE(index, val);
    }
    private void setUInt24BE(long index, UInt val) {
        int value = val.intValue();
        this.set(index    , (byte) (value >>> 16));
        this.set(index + 1, (byte) (value >>>  8));
        this.set(index + 2, (byte)  value);
    }
    private void setUInt24LE(long index, UInt val) {
        int value = val.intValue();
        this.set(index    , (byte)  value);
        this.set(index + 1, (byte) (value >>>  8));
        this.set(index + 2, (byte) (value >>> 16));
    }

    public void setUInt32(long index, UInt val, Endian endian) {
        if(endian == Endian.BIG) this.setUInt32BE(index, val);
        else this.setUInt32LE(index, val);
    }
    private void setUInt32BE(long index, UInt val) {
        int value = val.intValue();
        this.set(index    , (byte) (value >>> 24));
        this.set(index + 1, (byte) (value >>> 16));
        this.set(index + 2, (byte) (value >>>  8));
        this.set(index + 3, (byte)  value);
    }
    private void setUInt32LE(long index, UInt val) {
        int value = val.intValue();
        this.set(index    , (byte)  value);
        this.set(index + 1, (byte) (value >>>  8));
        this.set(index + 2, (byte) (value >>> 16));
        this.set(index + 3, (byte) (value >>> 24));
    }

    public void setUInt64(long index, ULong val, Endian endian) {
        if(endian == Endian.BIG) this.setUInt64BE(index, val);
        else this.setUInt64LE(index, val);
    }
    private void setUInt64BE(long index, ULong val) {
        long value = val.longValue();
        this.set(index    , (byte) (value >>> 56));
        this.set(index + 1, (byte) (value >>> 48));
        this.set(index + 2, (byte) (value >>> 40));
        this.set(index + 3, (byte) (value >>> 32));
        this.set(index + 4, (byte) (value >>> 24));
        this.set(index + 5, (byte) (value >>> 16));
        this.set(index + 6, (byte) (value >>>  8));
        this.set(index + 7, (byte)  value);
    }
    private void setUInt64LE(long index, ULong val) {
        long value = val.longValue();
        this.set(index    , (byte)  value);
        this.set(index + 1, (byte) (value >>>  8));
        this.set(index + 2, (byte) (value >>> 16));
        this.set(index + 3, (byte) (value >>> 24));
        this.set(index + 4, (byte) (value >>> 32));
        this.set(index + 5, (byte) (value >>> 40));
        this.set(index + 6, (byte) (value >>> 48));
        this.set(index + 7, (byte) (value >>> 56));
    }
}
