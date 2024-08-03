package com.mndk.bteterrarenderer.datatype.array;

import com.mndk.bteterrarenderer.core.util.BTRUtil;
import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

abstract class OwnedBigArray<E, EArray> implements BigArray<E> {

    private final long size;
    private final EArray[] arrays;

    OwnedBigArray(long size) {
        int outerLength = size == 0 ? 0 : (int) ((size - 1) / MAX_INNER_SIZE) + 1;

        this.size = size;
        this.arrays = this.createOuterArray(outerLength);
        long max = 0;
        for(int i = 0; i < outerLength; ++i) {
            max += MAX_INNER_SIZE;
            if(size >= max) arrays[i] = newInnerArray(MAX_INNER_SIZE);
            else arrays[i] = newInnerArray((int) (size - (max - MAX_INNER_SIZE)));
        }
    }
    OwnedBigArray(EArray array) {
        this.size = getInnerLength(array);
        // Since the size of the given array cannot exceed MAX_INNER_ARRAY_SIZE, the outer length can be 1
        this.arrays = this.createOuterArray(1);
        this.arrays[0] = array;
    }
    OwnedBigArray(BigArray<E> other) {
        this(other.size());
        this.copyTo(0, other, 0, this.size);
    }

    public E get(long index) {
        if(index < 0 || index >= this.size) throw new IndexOutOfBoundsException("size = " + this.size + ", index = " + index);
        return getFromInnerArray(arrays[(int) (index / MAX_INNER_SIZE)], (int) (index % MAX_INNER_SIZE));
    }

    public void set(long index, E value) {
        if(index < 0 || index >= this.size) throw new IndexOutOfBoundsException("size = " + this.size + ", index = " + index);
        setToInnerArray(arrays[(int) (index / MAX_INNER_SIZE)], (int) (index % MAX_INNER_SIZE), value);
    }

    public long size() {
        return this.size;
    }

    private void internalCopyTo(long srcIndex, OwnedBigArray<E, EArray> dest, long dstIndex, long size) {
        if(size == 0) return;
        if(srcIndex < 0 || srcIndex >= this.size) {
            throw new IndexOutOfBoundsException("size = " + this.size + ", index = " + srcIndex);
        }
        if(dstIndex < 0 || dstIndex >= dest.size()) {
            throw new IndexOutOfBoundsException("size = " + dest.size() + ", index = " + dstIndex);
        }
        if(size < 0) throw new IllegalArgumentException("Size must be non-negative.");
        if(srcIndex + size > this.size) throw new IndexOutOfBoundsException();
        if(dstIndex + size > dest.size()) throw new IndexOutOfBoundsException();

        int srcOuterIndex = (int) (srcIndex / MAX_INNER_SIZE);
        int srcInnerIndex = (int) (srcIndex % MAX_INNER_SIZE);
        int dstOuterIndex = (int) (dstIndex / MAX_INNER_SIZE);
        int dstInnerIndex = (int) (dstIndex % MAX_INNER_SIZE);

        while(size > 0) {
            int srcInnerLength = Math.min(getInnerLength(this.arrays[srcOuterIndex]) - srcInnerIndex, (int) size);
            int dstInnerLength = Math.min(getInnerLength(dest.arrays[dstOuterIndex]) - dstInnerIndex, (int) size);
            int length = Math.min(srcInnerLength, dstInnerLength);
            copyInnerArray(this.arrays[srcOuterIndex], srcInnerIndex, dest.arrays[dstOuterIndex], dstInnerIndex, length);
            size -= length;
            srcInnerIndex += length;
            if(srcInnerIndex >= MAX_INNER_SIZE) {
                srcOuterIndex++;
                srcInnerIndex -= MAX_INNER_SIZE;
            }
            dstInnerIndex += length;
            if(dstInnerIndex >= MAX_INNER_SIZE) {
                dstOuterIndex++;
                dstInnerIndex -= MAX_INNER_SIZE;
            }
        }
    }

    public void copyTo(long srcIndex, BigArray<E> dest, long dstIndex, long size) {
        if(dest instanceof OwnedBigArray) {
            this.internalCopyTo(srcIndex, (OwnedBigArray<E, EArray>) dest, dstIndex, size);
        } else {
            for(InnerArrayChunk<EArray> chunk : this.getInnerChunks(srcIndex, size)) {
                EArray array = chunk.array;
                for (int i = 0; i < chunk.length; ++i) {
                    dest.set(dstIndex + i, getFromInnerArray(array, chunk.offset + i));
                }
            }
        }
    }

    List<InnerArrayChunk<EArray>> getInnerChunks(long start, long length) {
        if(length < 0) throw new IllegalArgumentException("Length must be non-negative.");
        if(start + length > this.size) throw new IndexOutOfBoundsException();

        List<InnerArrayChunk<EArray>> result = new ArrayList<>();
        long end = start + length - 1;
        int outerStart = (int) (start / MAX_INNER_SIZE);
        int outerEnd = (int) (end / MAX_INNER_SIZE);
        for(int i = outerStart; i <= outerEnd; ++i) {
            int innerStart = i == outerStart ? (int) (start % MAX_INNER_SIZE) : 0;
            int innerEnd = i == outerEnd ? (int) (end % MAX_INNER_SIZE) : (MAX_INNER_SIZE - 1);
            int innerLength = innerEnd - innerStart + 1;
            result.add(new InnerArrayChunk<>(this.arrays[i], innerStart, innerLength));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof BigArray) {
            return this.equals(BTRUtil.uncheckedCast(obj));
        }
        return false;
    }

    public boolean equals(BigArray<E> other) {
        if(other == this) return true;
        if(other.size() != this.size) return false;
        if(other instanceof OwnedBigArray) {
            OwnedBigArray<E, EArray> casted = (OwnedBigArray<E, EArray>) other;
            return this.deepEquals(this.arrays, casted.arrays);
        }
        else {
            for(InnerArrayChunk<EArray> chunk : this.getInnerChunks(0, this.size)) {
                EArray array = chunk.array;
                for (int i = 0; i < chunk.length; ++i) {
                    E thisValue = getFromInnerArray(array, chunk.offset + i);
                    E otherValue = other.get(i);
                    if(!this.getElementType().equals(thisValue, otherValue)) return false;
                }
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        return this.hashCode(0, this.size);
    }

    @Override
    public int hashCode(long offset, long length) {
        if(offset < 0 || offset >= this.size) throw new IndexOutOfBoundsException();
        if(length < 0) throw new IllegalArgumentException("Length must be non-negative.");
        if(offset + length > this.size) throw new IndexOutOfBoundsException();
        int result = 1;
        DataType<E> elementType = this.getElementType();
        for(InnerArrayChunk<EArray> chunk : this.getInnerChunks(offset, length)) {
            EArray array = chunk.array;
            for (int i = 0; i < chunk.length; ++i) {
                E value = getFromInnerArray(array, chunk.offset + i);
                result = 31 * result + elementType.hashCode(value);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[length=" + this.size + "]";
    }

    protected EArray getFirstInnerArray() {
        return this.arrays[0];
    }

    protected abstract DataType<E> getElementType();
    protected abstract EArray newInnerArray(int length);
    protected abstract int getInnerLength(EArray array);
    protected abstract E getFromInnerArray(EArray array, int index);
    protected abstract void setToInnerArray(EArray array, int index, E value);
    protected abstract void copyInnerArray(EArray src, int srcIndex, EArray dest, int destIndex, int length);
    protected abstract EArray[] createOuterArray(int outerLength);
    protected abstract boolean deepEquals(EArray[] a, EArray[] b);

    @RequiredArgsConstructor
    private static class InnerArrayChunk<EArray> {
        public final EArray array;
        public final int offset;
        public final int length;
    }
}
