package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class PointIndex extends IndexTypeImpl<PointIndex> {
    // kInvalidPointIndex
    public static final PointIndex INVALID = new PointIndex(-1);

    private static final IndexTypeManager<PointIndex> ARRAY_MANAGER = PointIndex::new;
    public static DataType<PointIndex> type() { return ARRAY_MANAGER; }

    public static PointIndex of(int value) {
        return value == -1 ? INVALID : new PointIndex(value);
    }
    public static Iterable<PointIndex> range(int start, int until) {
        PointIndex startIdx = new PointIndex(start);
        PointIndex untilIdx = new PointIndex(until);
        return () -> startIdx.until(untilIdx);
    }

    private PointIndex(int value) { super(value); }
    @Override protected PointIndex newInstance(int value) { return new PointIndex(value); }
    @Override public boolean isInvalid() { return this.getValue() == INVALID.getValue(); }
}
