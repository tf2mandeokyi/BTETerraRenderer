package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class CornerIndex extends IndexTypeImpl<CornerIndex> {
    // kInvalidCornerIndex
    public static final CornerIndex INVALID = new CornerIndex(-1);

    private static final IndexTypeManager<CornerIndex> ARRAY_MANAGER = CornerIndex::new;
    public static DataType<CornerIndex> type() { return ARRAY_MANAGER; }

    public static CornerIndex of(int value) {
        return value == -1 ? INVALID : new CornerIndex(value);
    }
    public static Iterable<CornerIndex> range(int start, int until) {
        CornerIndex startIdx = new CornerIndex(start);
        CornerIndex untilIdx = new CornerIndex(until);
        return () -> startIdx.until(untilIdx);
    }

    private CornerIndex(int value) { super(value); }
    @Override protected CornerIndex newInstance(int value) { return new CornerIndex(value); }
    @Override public boolean isInvalid() { return this.getValue() == INVALID.getValue(); }
}
