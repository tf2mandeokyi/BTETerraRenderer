package com.mndk.bteterrarenderer.draco.attributes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttributeTransformType {
    ATTRIBUTE_INVALID_TRANSFORM(-1),
    ATTRIBUTE_NO_TRANSFORM(0),
    ATTRIBUTE_QUANTIZATION_TRANSFORM(1),
    ATTRIBUTE_OCTAHEDRON_TRANSFORM(2);

    private final int value;
}
