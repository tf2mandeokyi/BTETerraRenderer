package com.mndk.bteterrarenderer.draco.attributes;

import com.mndk.bteterrarenderer.draco.core.IndexType;

public class AttributeValueIndex extends IndexType<AttributeValueIndex> {
    // kInvalidAttributeValueIndex
    public static final AttributeValueIndex INVALID = new AttributeValueIndex(-1);

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
}
