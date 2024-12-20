package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.datatype.number.UByte;

public class BorrowedUByteArray extends AbstractBorrowedRawByteArray<UByte> {
    BorrowedUByteArray(byte[] array, int offset) { super(array, offset); }

    @Override public DataType<UByte> getType() { return DataType.uint8(); }
    @Override public Pointer<UByte> add(int offset) { return new BorrowedUByteArray(array, this.offset + offset); }

    @Override protected byte toRaw(UByte value) { return value.byteValue(); }
    @Override protected UByte fromRaw(byte raw) { return UByte.of(raw); }
    @Override public Pointer<UByte> toUByte() { return this; }
}
