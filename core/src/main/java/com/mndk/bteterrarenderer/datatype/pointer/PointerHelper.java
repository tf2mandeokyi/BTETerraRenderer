package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class PointerHelper {

    /**
     * Find the first byte index where the two raw pointers differ.
     * @param a The first raw pointer
     * @param b The second raw pointer
     * @param byteSize The number of bytes to compare
     * @return The first byte index where the two raw pointers differ, or -1 if they are equal
     */
    public long searchRawDifference(RawPointer a, RawPointer b, long byteSize) {
        for(long i = 0; i < byteSize; i++) if(a.getRawByte(i) != b.getRawByte(i)) return i;
        return -1;
    }

    public <T> long searchDifference(Pointer<T> a, Pointer<T> b, long elementCount) {
        DataType<T> type = a.getType();
        for(long i = 0; i < elementCount; i++) if(!type.equals(a.get(i), b.get(i))) return i;
        return -1;
    }

    public boolean rawContentEquals(RawPointer a, RawPointer b, long byteSize) {
        return searchRawDifference(a, b, byteSize) == -1;
    }

    public <T> boolean contentEquals(Pointer<T> a, Pointer<T> b, long elementCount) {
        return searchDifference(a, b, elementCount) == -1;
    }

    public <T> int contentHashCode(Pointer<T> pointer, long elementCount) {
        int hash = 0;
        DataType<T> type = pointer.getType();
        for(long i = 0; i < elementCount; i++) hash = 31 * hash + type.hashCode(pointer.get(i));
        return hash;
    }

    public <T> String contentToString(Pointer<T> pointer, long count) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for(long i = 0; i < count; i++) {
            builder.append(pointer.get(i));
            if(i < count - 1) builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }

    public <T> void sortContent(Pointer<T> pointer, long elementCount, Comparator<T> comparator) {
        sortContent(pointer, elementCount, comparator, true);
    }

    public <T> void sortContent(Pointer<T> pointer, long elementCount, Comparator<T> comparator, boolean stable) {
        if(stable) {
            PointerTimSort.sort(pointer, 0, elementCount, comparator, null, 0, 0);
        } else {
            PointerDualPivotQuicksort.sort(pointer, 0, elementCount - 1, comparator, null, 0, 0);
        }
    }

    public <T> boolean isContentSorted(Pointer<T> pointer, long elementCount, Comparator<T> comparator) {
        if(elementCount == 0) return true;
        T previous = pointer.get(0);
        for(long i = 1; i < elementCount; i++) {
            T current = pointer.get(i);
            if(comparator.compare(previous, current) > 0) return false;
            previous = current;
        }
        return true;
    }

    public <T> Iterator<T> iterator(Pointer<T> pointer, long elementCount) {
        return new PointerIterator<>(pointer, elementCount);
    }

    public <T> Stream<T> stream(Pointer<T> pointer, long elementCount) {
        Iterator<T> iterator = iterator(pointer, elementCount);
        Spliterator<T> spliterator = Spliterators.spliterator(iterator, elementCount, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    public String rawToString(RawPointer pointer, long byteSize) {
        byte[] bytes = new byte[(int) byteSize];
        rawCopy(pointer, bytes, 0, (int) byteSize);
        return new String(bytes);
    }

    public String rawToString(RawPointer pointer, long byteSize, Charset charset) {
        byte[] bytes = new byte[(int) byteSize];
        rawCopy(pointer, bytes, 0, (int) byteSize);
        return new String(bytes, charset);
    }

    // "Unsafe copy" methods directly copy data from src to dst,
    // without checking if the data is being overwritten.
    private void unsafeRawCopy(RawPointer src, RawPointer dst, long byteSize) {
        for(long i = 0; i < byteSize; i++) {
            dst.setRawByte(i, src.getRawByte(i));
        }
    }

    private <T> void unsafeCopyMultiple(Pointer<T> src, RawPointer dst, long srcElementCount) {
        DataType<T> type = src.getType();
        long elementByteSize = type.byteSize();
        for(long i = 0; i < srcElementCount; i++) {
            type.write(dst.rawAdd(i * elementByteSize), src.get(i));
        }
    }

    private <T> void unsafeCopyMultiple(RawPointer src, Pointer<T> dst, long dstElementCount) {
        DataType<T> type = dst.getType();
        long elementByteSize = type.byteSize();
        for(long i = 0; i < dstElementCount; i++) {
            dst.set(i, type.read(src.rawAdd(i * elementByteSize)));
        }
    }

    private <T> void unsafeCopyMultiple(Pointer<T> src, Pointer<T> dst, long elementCount) {
        for(long i = 0; i < elementCount; i++) {
            dst.set(i, src.get(i));
        }
    }

    // We do not directly copy data from src to dst, since if both the src and dst had a same origin,
    // the copy operation would not work correctly due to the fact that the data would be overwritten.
    public void rawCopy(RawPointer src, RawPointer dst, long byteSize) {
        if(src.getOrigin() == dst.getOrigin()) {
            RawPointer temp = DataType.uint8().newArray(byteSize).asRaw();
            unsafeRawCopy(src, temp, byteSize);
            unsafeRawCopy(temp, dst, byteSize);
        } else {
            unsafeRawCopy(src, dst, byteSize);
        }
    }

    public void rawCopy(byte[] src, int srcOffset, RawPointer dst, int byteSize) {
        for(int i = 0; i < byteSize; i++) dst.setRawByte(i, src[srcOffset + i]);
    }

    public void rawCopy(RawPointer src, byte[] dst, int dstOffset, int byteSize) {
        for(int i = 0; i < byteSize; i++) dst[dstOffset + i] = src.getRawByte(i);
    }

    public <T> void copyMultiple(Pointer<T> src, Pointer<T> dst, long elementCount) {
        if(src.getOrigin() == dst.getOrigin()) {
            DataType<T> type = src.getType();
            Pointer<T> temp = type.newArray(elementCount);
            unsafeCopyMultiple(src, temp, elementCount);
            unsafeCopyMultiple(temp, dst, elementCount);
        } else {
            unsafeCopyMultiple(src, dst, elementCount);
        }
    }

    public <T> void copySingle(RawPointer src, Pointer<T> dst) {
        DataType<T> type = dst.getType();
        if(src.getOrigin() == dst.getOrigin()) {
            long byteSize = type.byteSize();
            RawPointer temp = DataType.uint8().newArray(byteSize).asRaw();
            unsafeRawCopy(src, temp, byteSize);
            dst.set(type.read(temp));
        } else {
            dst.set(type.read(src));
        }
    }

    public <T> void copyMultiple(RawPointer src, Pointer<T> dst, long dstElementCount) {
        if(src.getOrigin() == dst.getOrigin()) {
            DataType<T> type = dst.getType();
            long elementByteSize = type.byteSize();
            RawPointer temp = DataType.uint8().newArray(elementByteSize * dstElementCount).asRaw();
            unsafeRawCopy(src, temp, elementByteSize * dstElementCount);
            unsafeCopyMultiple(temp, dst, dstElementCount);
        } else {
            unsafeCopyMultiple(src, dst, dstElementCount);
        }
    }

    public <T> void copySingle(Pointer<T> src, RawPointer dst) {
        DataType<T> type = src.getType();
        if(src.getOrigin() == dst.getOrigin()) {
            long byteSize = type.byteSize();
            RawPointer temp = DataType.uint8().newArray(byteSize).asRaw();
            type.write(temp, src.get());
            unsafeRawCopy(temp, dst, byteSize);
        } else {
            type.write(dst, src.get());
        }
    }

    public <T> void copyMultiple(Pointer<T> src, RawPointer dst, long srcElementCount) {
        if(src.getOrigin() == dst.getOrigin()) {
            long elementByteSize = src.getType().byteSize();
            RawPointer temp = DataType.uint8().newArray(elementByteSize * srcElementCount).asRaw();
            unsafeCopyMultiple(src, temp, srcElementCount);
            unsafeRawCopy(temp, dst, elementByteSize * srcElementCount);
        } else {
            unsafeCopyMultiple(src, dst, srcElementCount);
        }
    }

    public <T> void reverse(Pointer<T> pointer, long length) {
        for(long i = 0; i < length / 2; i++) {
            pointer.swap(i, length - i - 1);
        }
    }

    public <T> boolean nextPermutation(Pointer<T> pointer, long elementCount, @Nullable Comparator<T> comparator) {
        if(elementCount <= 1) return false;

        Comparator<T> realComparator = comparator != null ? comparator : pointer.getType().asNumber()::compareTo;
        long i = elementCount - 1, j = elementCount - 1;

        while(i > 0 && realComparator.compare(pointer.get(i - 1), pointer.get(i)) >= 0) i--;
        if(i == 0) return false;

        while(realComparator.compare(pointer.get(i - 1), pointer.get(j)) >= 0) j--;
        pointer.swap(i - 1, j);

        j = elementCount - 1;
        for(; i < j; i++, j--) {
            pointer.swap(i, j);
        }
        return true;
    }

}
