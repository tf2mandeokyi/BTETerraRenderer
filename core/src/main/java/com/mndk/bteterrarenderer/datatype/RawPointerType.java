package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.array.BigUByteArray;
import com.mndk.bteterrarenderer.datatype.pointer.PointerHelper;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

import java.nio.charset.StandardCharsets;

class RawPointerType extends ObjectType<RawPointer> {
    private final long size;

    public RawPointerType(long size) {
        super(() -> BigUByteArray.create(size).getRawPointer());
        this.size = size;
    }

    // Java overrides
    @Override public String toString() { return "bytes[" + size + "]"; }
    @Override public int hashCode() { return (int) size; }
    @Override public boolean equals(Object obj) {
        return obj instanceof RawPointerType && ((RawPointerType) obj).size == size;
    }

    // IO operations
    @Override public long byteSize() { return size; }
    @Override public RawPointer read(RawPointer src) { return BigUByteArray.create(src, size).getRawPointer(); }
    @Override public void write(RawPointer dst, RawPointer value) { PointerHelper.rawCopy(value, dst, size); }

    // General conversions
    @Override public RawPointer parse(String value) {
        return BigUByteArray.create(value, StandardCharsets.UTF_8).getRawPointer();
    }
}
