package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.IndexType;

public class VertexIndex extends IndexType<VertexIndex> {
    // kInvalidVertexIndex
    public static final VertexIndex INVALID = new VertexIndex(-1);

    public static VertexIndex of(int value) {
        return new VertexIndex(value);
    }

    private VertexIndex(int value) {
        super(value);
    }

    @Override
    protected VertexIndex newInstance(int value) {
        return new VertexIndex(value);
    }
}
