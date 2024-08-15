package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.mndk.bteterrarenderer.datatype.DataType.fromRaw;
import static com.mndk.bteterrarenderer.datatype.DataType.toRaw;

@Getter
@RequiredArgsConstructor
class BorrowedBooleanArray extends BorrowedArray<Boolean> implements RawBytePointer {
    private final boolean[] array;
    private final int offset;

    @Override public DataType<Boolean> getType() { return DataType.bool(); }
    @Override public Boolean get() { return array[offset]; }
    @Override public void set(Boolean value) { array[offset] = value; }
    @Override public RawPointer asRaw() { return this; }
    @Override protected Boolean get(int index) { return array[this.offset + index]; }
    @Override protected void set(int index, Boolean value) { array[this.offset + index] = value; }
    @Override protected Pointer<Boolean> add(int offset) { return new BorrowedBooleanArray(array, this.offset + offset); }
    @Override public void swap(int a, int b) {
        boolean temp = array[offset + a];
        array[offset + a] = array[offset + b];
        array[offset + b] = temp;
    }

    @Override public byte getRawByte(long index) { return toRaw(array[checkIndex(offset + index)]); }
    @Override public void setRawByte(long index, byte value) { array[checkIndex(offset + index)] = fromRaw(value); }
    @Override public Pointer<Boolean> toBool() { return this; }
}
