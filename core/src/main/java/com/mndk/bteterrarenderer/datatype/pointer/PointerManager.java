package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.array.BigUByteArray;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PointerManager {

    /**
     * Find the first byte index where the two raw pointers differ.
     * @param a The first raw pointer
     * @param b The second raw pointer
     * @param byteSize The number of bytes to compare
     * @return The first byte index where the two raw pointers differ, or -1 if they are equal
     */
    public long searchDifference(RawPointer a, RawPointer b, long byteSize) {
        for(long i = 0; i < byteSize; i++) if(a.getRawByte(i) != b.getRawByte(i)) return i;
        return -1;
    }

    public boolean contentEquals(RawPointer a, RawPointer b, long byteSize) {
        return searchDifference(a, b, byteSize) == -1;
    }

    public void unsafeCopy(RawPointer src, RawPointer dst, long byteSize) {
        // This method is unsafe since it directly copies data from src to dst.
        for(long i = 0; i < byteSize; i++) dst.setRawByte(i, src.getRawByte(i));
    }

    public void copy(RawPointer src, RawPointer dst, long byteSize) {
        // We do not directly copy data from src to dst, since if both the src and dst had a same origin,
        // the copy operation would not work correctly due to the fact that the data would be overwritten.
        BigUByteArray temp = BigUByteArray.create(byteSize);
        unsafeCopy(src, temp.getRawPointer(), byteSize);
        unsafeCopy(temp.getRawPointer(), dst, byteSize);
    }

    public void copy(byte[] src, int srcOffset, RawPointer dst, int byteSize) {
        for(int i = 0; i < byteSize; i++) dst.setRawByte(i, src[srcOffset + i]);
    }

    public void copy(RawPointer src, byte[] dst, int dstOffset, int byteSize) {
        for(int i = 0; i < byteSize; i++) dst[dstOffset + i] = src.getRawByte(i);
    }

    public <T> void copy(RawPointer src, Pointer<T> dst) {
        // We do not directly copy data from src to dst, since if both the src and dst had a same origin,
        // the copy operation would not work correctly due to the fact that the data would be overwritten.
        DataType<T> type = dst.getType();
        long byteSize = type.byteSize();
        BigUByteArray temp = BigUByteArray.create(byteSize);
        unsafeCopy(src, temp.getRawPointer(), byteSize);
        dst.set(type.read(temp.getRawPointer()));
    }

    public <T> void copy(RawPointer src, Pointer<T> dst, long elementCount) {
        // We do not directly copy data from src to dst, since if both the src and dst had a same origin,
        // the copy operation would not work correctly due to the fact that the data would be overwritten.
        DataType<T> type = dst.getType();
        long elementByteSize = type.byteSize();
        BigUByteArray temp = BigUByteArray.create(elementByteSize * elementCount);
        for(long i = 0; i < elementByteSize * elementCount; i++) temp.set(i, src.getRawByte(i));
        for(long i = 0; i < elementCount; i++) dst.set(i, type.read(temp.getRawPointer(i * elementByteSize)));
    }

    public <T> void copy(Pointer<T> src, RawPointer dst) {
        // We do not directly copy data from src to dst, since if both the src and dst had a same origin,
        // the copy operation would not work correctly due to the fact that the data would be overwritten.
        DataType<T> type = src.getType();
        long byteSize = type.byteSize();
        BigUByteArray temp = BigUByteArray.create(byteSize);
        type.write(temp.getRawPointer(), src.get());
        for(long i = 0; i < byteSize; i++) dst.setRawByte(i, temp.get(i));
    }

    public <T> void copy(Pointer<T> src, RawPointer dst, long count) {
        // We do not directly copy data from src to dst, since if both the src and dst had a same origin,
        // the copy operation would not work correctly due to the fact that the data would be overwritten.
        DataType<T> type = src.getType();
        long elementByteSize = type.byteSize();
        BigUByteArray temp = BigUByteArray.create(elementByteSize * count);
        for(long i = 0; i < count; i++) type.write(temp.getRawPointer(i * elementByteSize), src.get(i));
        for(long i = 0; i < elementByteSize * count; i++) dst.setRawByte(i, temp.get(i));
    }

}
