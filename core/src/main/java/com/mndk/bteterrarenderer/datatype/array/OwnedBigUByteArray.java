package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.core.util.IOUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.RawBytePointer;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

class OwnedBigUByteArray extends OwnedBigArray<UByte, byte[]> implements BigUByteArray {

    OwnedBigUByteArray(long size) { super(size); }
    OwnedBigUByteArray(byte[] value) { super(value); }
    OwnedBigUByteArray(BigUByteArray other) { super(other); }
    OwnedBigUByteArray(String value, Charset charset) { super(value.getBytes(charset)); }
    OwnedBigUByteArray(InputStream stream) throws IOException { super(IOUtil.readAllBytes(stream)); }
    OwnedBigUByteArray(ByteBuf buf) { super(IOUtil.readAllBytes(buf)); }
    OwnedBigUByteArray(ByteBuffer buffer) { super(IOUtil.readAllBytes(buffer)); }
    OwnedBigUByteArray(RawPointer src, long byteLength) {
        super(byteLength);
        for(long i = 0; i < byteLength; i++) this.set(i, src.getRawByte((int) i));
    }

    public void set(long index, byte value) { this.set(index, UByte.of(value)); }

    @Override protected DataType<UByte> getElementType() { return DataType.uint8(); }
    @Override protected byte[] newInnerArray(int length) { return new byte[length];}
    @Override protected int getInnerLength(byte[] array) { return array.length; }
    @Override protected UByte getFromInnerArray(byte[] array, int index) { return UByte.of(array[index]);}
    @Override protected void setToInnerArray(byte[] array, int index, UByte value) { array[index] = value.byteValue(); }
    @Override protected void copyInnerArray(byte[] src, int srcIndex, byte[] dest, int destIndex, int length) {
        System.arraycopy(src, srcIndex, dest, destIndex, length);
    }
    @Override protected byte[][] createOuterArray(int outerLength) { return new byte[outerLength][]; }
    @Override protected boolean deepEquals(byte[][] a, byte[][] b) { return Arrays.deepEquals(a, b); }

    public String decode(long offset, int length, Charset charset) {
        int maxAllowedSize = (int) Math.min(this.size() - offset, MAX_INNER_SIZE);
        if(maxAllowedSize < length) {
            throw new IllegalArgumentException("Expected size argument less than or equal to " + maxAllowedSize +
                    ", instead got " + length);
        }
        byte[] result = new byte[length];
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

    @Override public RawPointer getRawPointer(long byteOffset) { return new UByteArrayRawPointer(byteOffset); }

    @RequiredArgsConstructor
    private class UByteArrayRawPointer implements RawBytePointer, Pointer<UByte> {
        private final long offset;

        @Override public String toString() {
            int origin = System.identityHashCode(OwnedBigUByteArray.this);
            return "Pointer[" + String.format("%08x", origin) + "+" + offset + "]";
        }
        @Override public boolean equals(Object obj) {
            return obj instanceof UByteArrayRawPointer && ((UByteArrayRawPointer) obj).offset == this.offset;
        }
        @Override public int hashCode() {
            return Long.hashCode(offset);
        }

        @Override public DataType<UByte> getType() { return DataType.uint8(); }
        @Override public UByte get() { return OwnedBigUByteArray.this.get(offset);}
        @Override public UByte get(long index) { return OwnedBigUByteArray.this.get(offset + index);}
        @Override public void set(UByte value) { OwnedBigUByteArray.this.set(offset, value); }
        @Override public void set(long index, UByte value) { OwnedBigUByteArray.this.set(offset + index, value); }
        @Override public Pointer<UByte> add(long offset) { return new UByteArrayRawPointer(this.offset + offset); }
        @Override public Object getOrigin() { return OwnedBigUByteArray.this; }
        @Override public RawPointer asRaw() { return this; }

        @Override public byte getRawByte(long index) { return OwnedBigUByteArray.this.get(offset + index).byteValue(); }
        @Override public UByte getRawUByte(long index) { return OwnedBigUByteArray.this.get(offset + index); }
        @Override public void setRawByte(long index, byte value) { OwnedBigUByteArray.this.set(offset + index, value); }
        @Override public void setRawByte(long byteIndex, UByte value) { OwnedBigUByteArray.this.set(offset + byteIndex, value); }
        @Override public Pointer<UByte> toUByte() { return this; }
    }
}
