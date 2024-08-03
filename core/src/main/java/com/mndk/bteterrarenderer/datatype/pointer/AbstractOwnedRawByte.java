package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractOwnedRawByte<E> extends SingleVariablePointer<E> implements RawBytePointer {
    private byte value;

    @Override public final E get() { return fromRaw(value); }
    @Override public final E get(long index) { checkIndex(index); return fromRaw(value); }
    @Override public final void set(E value) { this.value = toRaw(value); }
    @Override public final void set(long index, E value) { checkIndex(index); this.value = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }

    protected abstract byte toRaw(E value);
    protected abstract E fromRaw(byte raw);
    @Override public final byte getRawByte(long index) { checkIndex(index); return value; }
    @Override public final void setRawByte(long index, byte value) { checkIndex(index); this.value = value; }
}
