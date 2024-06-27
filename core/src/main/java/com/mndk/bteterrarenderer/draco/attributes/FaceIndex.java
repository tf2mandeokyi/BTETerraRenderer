package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import com.mndk.bteterrarenderer.draco.core.IndexType;

public class FaceIndex extends IndexType<FaceIndex> {
    // kInvalidFaceIndex
    public static final FaceIndex INVALID = new FaceIndex(-1);

    private static final IndexArrayManager<FaceIndex> ARRAY_MANAGER = FaceIndex::new;
    public static DataArrayManager<FaceIndex, int[]> arrayManager() { return ARRAY_MANAGER; }

    public static FaceIndex of(int value) {
        return new FaceIndex(value);
    }
    public static Iterable<FaceIndex> range(int start, int until) {
        FaceIndex startIdx = new FaceIndex(start);
        FaceIndex untilIdx = new FaceIndex(until);
        return () -> startIdx.until(untilIdx);
    }

    private FaceIndex(int value) {
        super(value);
    }

    @Override
    protected FaceIndex newInstance(int value) {
        return new FaceIndex(value);
    }

    @Override
    public boolean isInvalid() {
        return this.getValue() == INVALID.getValue();
    }
}
