package com.mndk.bteterrarenderer.datatype;

import com.mndk.bteterrarenderer.datatype.array.BigUByteArray;
import com.mndk.bteterrarenderer.datatype.pointer.RawPointer;

import java.nio.charset.Charset;

class StringType extends ObjectType<String> {
    private final int byteLength;
    private final Charset charset;

    public StringType(int byteLength, Charset charset) {
        super(String::new);
        this.byteLength = byteLength;
        this.charset = charset;
    }

    // Java overrides
    @Override public String toString() { return "str_bytes[" + byteLength + "]"; }
    @Override public int hashCode() { return byteLength; }
    @Override public boolean equals(Object obj) {
        return obj instanceof StringType && ((StringType) obj).byteLength == byteLength;
    }

    // IO operations
    @Override public long byteSize() { return byteLength; }
    @Override public String read(RawPointer src) { return BigUByteArray.create(src, byteLength).decode(charset); }
    @Override public void write(RawPointer dst, String value) {
        BigUByteArray.create(value, charset).getRawPointer().rawCopyTo(dst, byteLength);
    }

    // General conversions
    @Override public String parse(String value) { return value; }
}
