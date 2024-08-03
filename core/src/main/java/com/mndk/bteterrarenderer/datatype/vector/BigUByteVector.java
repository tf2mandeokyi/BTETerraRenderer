package com.mndk.bteterrarenderer.datatype.vector;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.BigArray;
import com.mndk.bteterrarenderer.datatype.array.BigUByteArray;
import com.mndk.bteterrarenderer.datatype.number.UByte;
import com.mndk.bteterrarenderer.datatype.pointer.Pointer;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

import java.nio.charset.Charset;
import java.util.Objects;

public class BigUByteVector implements BigUByteArray {

    private BigUByteArray array;
    private long size;

    public BigUByteVector() {
        this(BigUByteArray.EMPTY);
    }
    public BigUByteVector(BigUByteArray array) {
        this.array = array;
        this.size = array.size();
    }
    public BigUByteVector(long size) {
        this(BigUByteArray.create(size));
    }

    public void reserve(long minCapacity) {
        long oldCapacity = array.size();
        if(oldCapacity >= minCapacity) return;

        long newCapacity = oldCapacity + oldCapacity >> 1;
        if(newCapacity < minCapacity) newCapacity = minCapacity;
        BigUByteArray newArray = BigUByteArray.create(newCapacity);
        array.copyTo(0, newArray, 0, size);
        array = newArray;
    }

    public void resize(long size) {
        if(size > this.size) this.reserve(size);
        this.size = size;
    }

    public <T> void insert(long index, Pointer<T> in, long count) {
        DataType<T> type = in.getType();
        long elementByteCount = type.byteSize(), totalByteCount = type.byteSize() * count;
        reserve(this.size + totalByteCount);
        array.copyTo(index, array, index + totalByteCount, this.size - index);
        for(long i = 0; i < count; i++) {
            type.write(array.getRawPointer(size + i * elementByteCount), in.get(i));
        }
        size += totalByteCount;
    }

    public void insertRaw(long index, RawPointer in, long byteCount) {
        reserve(this.size + byteCount);
        array.copyTo(index, array, index + byteCount, this.size - index);
        for(long i = 0; i < byteCount; i++) {
            array.set(index + i, in.getRawByte(i));
        }
        size += byteCount;
    }

    @Override public long size() { return this.size; }
    @Override public void set(long index, byte value) { checkIndex(index); array.set(index, value); }
    @Override public UByte get(long index) { checkIndex(index); return array.get(index); }
    @Override public void set(long index, UByte value) { checkIndex(index); array.set(index, value); }

    @Override public boolean equals(BigArray<UByte> other) { return array.equals(other); }
    @Override public void copyTo(long srcIndex, BigArray<UByte> dest, long dstIndex, long size) {
        checkRange(srcIndex, size);
        array.copyTo(srcIndex, dest, dstIndex, size);
    }
    @Override public int hashCode(long offset, long length) {
        checkRange(offset, length);
        return array.hashCode(offset, length);
    }

    @Override public RawPointer getRawPointer(long byteOffset) { return array.getRawPointer(byteOffset); }

    @Override public String decode(long offset, int length, Charset charset) {
        checkRange(offset, length);
        return array.decode(offset, length, charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, array.hashCode(0, size));
    }

    private void checkIndex(long index) {
        if(index < this.size) return;
        throw new IndexOutOfBoundsException("index = " + index + ", size = " + this.size);
    }

    private void checkRange(long offset, long length) {
        if(length <= this.size - offset) return;
        throw new IndexOutOfBoundsException("offset = " + offset + ", length = " + length + ", size = " + this.size);
    }
}
