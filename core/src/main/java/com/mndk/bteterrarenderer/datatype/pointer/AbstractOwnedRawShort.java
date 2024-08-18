package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractOwnedRawShort<E> extends SingleVariablePointer<E> implements RawShortPointer {
    protected short value;

    @Override public final E get() { return fromRaw(value); }
    @Override public final void set(E value) { this.value = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }

    protected abstract short toRaw(E value);
    protected abstract E fromRaw(short raw);
    @Override public final short getRawShort(long index) { checkIndex(index); return value; }
    @Override public final void setRawShort(long index, short value) { checkIndex(index); this.value = value; }
}
