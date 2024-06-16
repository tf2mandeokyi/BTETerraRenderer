package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.IndexType;

public class CornerIndex extends IndexType<CornerIndex> {
    // kInvalidCornerIndex
    public static final CornerIndex INVALID = new CornerIndex(-1);

    public static CornerIndex of(int value) {
        return new CornerIndex(value);
    }

    private CornerIndex(int value) {
        super(value);
    }

    @Override
    protected CornerIndex newInstance(int value) {
        return new CornerIndex(value);
    }
}
