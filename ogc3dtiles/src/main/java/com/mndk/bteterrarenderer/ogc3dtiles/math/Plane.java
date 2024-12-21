package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.Getter;

@Getter
public class Plane {
    private final Cartesian3f point;
    private final Cartesian3f normal;

    public Plane(Cartesian3f point, Cartesian3f normal) {
        this.point = point;
        this.normal = normal.toNormalized();
    }
}
