package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BigArrayView<E, EBigArray extends BigArray<E>> implements BigArray<E> {

    protected final EBigArray array;
    protected final long offset;

    @Override public E get(long index) { return array.get(index + offset); }
    @Override public void set(long index, E value) { array.set(index + offset, value); }
    @Override public long size() { return array.size() - offset; }

    @Override public void copyTo(long srcIndex, BigArray<E> dest, long dstIndex, long size) {
        array.copyTo(srcIndex + offset, dest, dstIndex, size);
    }

    @Override public RawPointer getRawPointer(long byteOffset) { return array.getRawPointer(this.offset + byteOffset); }

    @Override public boolean equals(BigArray<E> other) {
        if(other.size() != this.size()) return false;
        for(long i = 0; i < this.size(); ++i) {
            if(!this.get(i).equals(other.get(i))) return false;
        }
        return true;
    }

    @Override public int hashCode() { return array.hashCode(offset, this.size()); }
    @Override public int hashCode(long offset, long length) { return array.hashCode(this.offset + offset, length); }
}
