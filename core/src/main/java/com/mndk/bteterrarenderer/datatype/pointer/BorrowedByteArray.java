package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;

class BorrowedByteArray extends AbstractBorrowedRawByteArray<Byte> {
    BorrowedByteArray(byte[] array, int offset) { super(array, offset); }

    @Override public DataType<Byte> getType() { return DataType.int8(); }
    @Override public Pointer<Byte> add(int offset) { return new BorrowedByteArray(array, this.offset + offset); }

    @Override protected byte toRaw(Byte value) { return value; }
    @Override protected Byte fromRaw(byte raw) { return raw; }
    @Override public Pointer<Byte> toByte() { return this; }
}
