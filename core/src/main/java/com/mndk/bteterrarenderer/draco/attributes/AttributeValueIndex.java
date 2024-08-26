package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataType;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class AttributeValueIndex extends IndexTypeImpl<AttributeValueIndex> {
    // kInvalidAttributeValueIndex
    public static final AttributeValueIndex INVALID = new AttributeValueIndex(-1);

    private static final IndexTypeManager<AttributeValueIndex> ARRAY_MANAGER = AttributeValueIndex::of;
    public static DataType<AttributeValueIndex> type() { return ARRAY_MANAGER; }

    public static AttributeValueIndex of(int value) {
        return value == -1 ? INVALID : new AttributeValueIndex(value);
    }
    public static Iterable<AttributeValueIndex> range(int start, int until) {
        AttributeValueIndex startIdx = of(start);
        AttributeValueIndex untilIdx = of(until);
        return () -> startIdx.until(untilIdx);
    }

    private AttributeValueIndex(int value) { super(value); }
    @Override protected AttributeValueIndex newInstance(int value) { return of(value); }
    @Override public boolean isInvalid() { return this.getValue() == INVALID.getValue(); }
}
