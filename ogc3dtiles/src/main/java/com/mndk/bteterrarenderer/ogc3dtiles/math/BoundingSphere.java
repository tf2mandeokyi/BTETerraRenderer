package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3d;

@Getter
@RequiredArgsConstructor
public class BoundingSphere {
    private final Vector3d center;
    private final double radius;
}
