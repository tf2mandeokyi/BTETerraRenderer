package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BoundingSphere {
    private final Cartesian3f center;
    private final float radius;
}
