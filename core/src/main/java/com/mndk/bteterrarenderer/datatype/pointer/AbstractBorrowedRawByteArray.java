package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractBorrowedRawByteArray<E> extends BorrowedArray<E> implements RawBytePointer {
    protected final byte[] array;
    protected final int offset;

    @Override public final E get() { return fromRaw(array[offset]); }
    @Override public final void set(E value) { array[offset] = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }
    @Override protected final E get(int index) { return fromRaw(array[this.offset + index]); }
    @Override protected final void set(int index, E value) { array[this.offset + index] = toRaw(value); }
    @Override protected final void swap(int a, int b) {
        byte temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    protected abstract byte toRaw(E value);
    protected abstract E fromRaw(byte raw);
    @Override public final byte getRawByte(long index) { return array[DataType.intLimit(offset + index)]; }
    @Override public final void setRawByte(long index, byte value) { array[DataType.intLimit(offset + index)] = value; }
}
