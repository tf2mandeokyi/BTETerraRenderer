package com.mndk.bteterrarenderer.datatype.pointer;

import com.mndk.bteterrarenderer.datatype.DataType;

class OwnedInt extends AbstractOwnedRawInt<Integer> {
    OwnedInt(int value) { super(value); }

    @Override public DataType<Integer> getType() { return DataType.int32(); }
    @Override protected int toRaw(Integer value) { return value; }
    @Override protected Integer fromRaw(int raw) { return raw; }
    @Override public Pointer<Integer> toInt() { return this; }
}
