package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class VertexIndex extends IndexTypeImpl<VertexIndex> {
    // kInvalidVertexIndex
    public static final VertexIndex INVALID = new VertexIndex(-1);

    private static final IndexArrayManager<VertexIndex> ARRAY_MANAGER = VertexIndex::new;
    public static DataArrayManager<VertexIndex, int[]> arrayManager() { return ARRAY_MANAGER; }

    public static VertexIndex of(int value) {
        return new VertexIndex(value);
    }
    public static Iterable<VertexIndex> range(int start, int until) {
        VertexIndex startIdx = new VertexIndex(start);
        VertexIndex untilIdx = new VertexIndex(until);
        return () -> startIdx.until(untilIdx);
    }

    private VertexIndex(int value) {
        super(value);
    }

    @Override
    protected VertexIndex newInstance(int value) {
        return new VertexIndex(value);
    }

    @Override
    public boolean isInvalid() {
        return this.getValue() == INVALID.getValue();
    }
}
