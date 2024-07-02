package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.datatype.DataArrayManager;
import com.mndk.bteterrarenderer.draco.core.IndexTypeImpl;

public class AttributeValueIndex extends IndexTypeImpl<AttributeValueIndex> {
    // kInvalidAttributeValueIndex
    public static final AttributeValueIndex INVALID = new AttributeValueIndex(-1);

    private static final IndexArrayManager<AttributeValueIndex> ARRAY_MANAGER = AttributeValueIndex::new;
    public static DataArrayManager<AttributeValueIndex, int[]> arrayManager() { return ARRAY_MANAGER; }

    public static AttributeValueIndex of(int value) {
        return new AttributeValueIndex(value);
    }

    private AttributeValueIndex(int value) {
        super(value);
    }

    @Override
    protected AttributeValueIndex newInstance(int value) {
        return new AttributeValueIndex(value);
    }

    @Override
    public boolean isInvalid() {
        return this.getValue() == INVALID.getValue();
    }
}
