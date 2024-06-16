package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.IndexType;

public class PointIndex extends IndexType<PointIndex> {
    // kInvalidPointIndex
    public static final PointIndex INVALID = new PointIndex(-1);

    public static PointIndex of(int value) {
        return new PointIndex(value);
    }

    private PointIndex(int value) {
        super(value);
    }

    @Override
    protected PointIndex newInstance(int value) {
        return new PointIndex(value);
    }
}
