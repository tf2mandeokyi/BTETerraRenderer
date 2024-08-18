package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractOwnedRawInt<E> extends SingleVariablePointer<E> implements RawIntPointer {
    protected int value;

    @Override public final E get() { return fromRaw(value); }
    @Override public final void set(E value) { this.value = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }

    protected abstract int toRaw(E value);
    protected abstract E fromRaw(int raw);
    @Override public final int getRawInt(long index) { checkIndex(index); return value; }
    @Override public final void setRawInt(long index, int value) { checkIndex(index); this.value = value; }
}
