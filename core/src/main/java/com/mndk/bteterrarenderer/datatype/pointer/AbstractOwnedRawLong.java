package com.mndk.bteterrarenderer.datatype.pointer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractOwnedRawLong<E> extends SingleVariablePointer<E> implements RawLongPointer {
    protected long value;

    @Override public final E get() { return fromRaw(value); }
    @Override public final void set(E value) { this.value = toRaw(value); }
    @Override public final RawPointer asRaw() { return this; }

    protected abstract long toRaw(E value);
    protected abstract E fromRaw(long raw);
    @Override public final long getRawLong(long index) { checkIndex(index); return value; }
    @Override public final void setRawLong(long index, long value) { checkIndex(index); this.value = value; }
}
