package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class FaceIndex extends IndexTypeImpl<FaceIndex> {
    // kInvalidFaceIndex
    public static final FaceIndex INVALID = new FaceIndex(-1);

    private static final IndexTypeManager<FaceIndex> ARRAY_MANAGER = FaceIndex::new;
    public static DataType<FaceIndex> type() { return ARRAY_MANAGER; }

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
