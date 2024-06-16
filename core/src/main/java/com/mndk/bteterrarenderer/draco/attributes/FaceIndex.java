package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.IndexType;

public class FaceIndex extends IndexType<FaceIndex> {
    // kInvalidFaceIndex
    public static final FaceIndex INVALID = new FaceIndex(-1);

    public static FaceIndex of(int value) {
        return new FaceIndex(value);
    }

    private FaceIndex(int value) {
        super(value);
    }

    @Override
    protected FaceIndex newInstance(int value) {
        return new FaceIndex(value);
    }
}
