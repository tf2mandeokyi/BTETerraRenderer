package com.mndk.bteterrarenderer.draco.attributes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AttributeTransformType {
    INVALID(-1),
    NONE(0),
    QUANTIZATION(1),
    OCTAHEDRON(2);

    private final int value;
}
