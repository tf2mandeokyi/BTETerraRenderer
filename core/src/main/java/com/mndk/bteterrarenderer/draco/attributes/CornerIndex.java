package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import com.mndk.bteterrarenderer.draco.core.IndexType;

public class CornerIndex extends IndexType<CornerIndex> {
    // kInvalidCornerIndex
    public static final CornerIndex INVALID = new CornerIndex(-1);

    private static final IndexArrayManager<CornerIndex> ARRAY_MANAGER = CornerIndex::new;
    public static DataArrayManager<CornerIndex, int[]> arrayManager() { return ARRAY_MANAGER; }

    public static CornerIndex of(int value) {
        return new CornerIndex(value);
    }
    public static Iterable<CornerIndex> range(int start, int until) {
        CornerIndex startIdx = new CornerIndex(start);
        CornerIndex untilIdx = new CornerIndex(until);
        return () -> startIdx.until(untilIdx);
    }

    private CornerIndex(int value) {
        super(value);
    }

    @Override
    protected CornerIndex newInstance(int value) {
        return new CornerIndex(value);
    }

    @Override
    public boolean isInvalid() {
        return this.getValue() == INVALID.getValue();
    }
}
