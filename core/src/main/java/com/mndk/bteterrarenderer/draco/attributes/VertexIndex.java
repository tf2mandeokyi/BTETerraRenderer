package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class VertexIndex extends IndexTypeImpl<VertexIndex> {
    // kInvalidVertexIndex
    public static final VertexIndex INVALID = new VertexIndex(-1);

    private static final IndexTypeManager<VertexIndex> ARRAY_MANAGER = VertexIndex::of;
    public static DataType<VertexIndex> type() { return ARRAY_MANAGER; }

    public static VertexIndex of(int value) {
        return value == -1 ? INVALID : new VertexIndex(value);
    }
    public static Iterable<VertexIndex> range(int start, int until) {
        VertexIndex startIdx = of(start);
        VertexIndex untilIdx = of(until);
        return () -> startIdx.until(untilIdx);
    }

    private VertexIndex(int value) { super(value); }
    @Override protected VertexIndex newInstance(int value) { return of(value); }
    @Override public boolean isInvalid() { return this.getValue() == INVALID.getValue(); }
}
