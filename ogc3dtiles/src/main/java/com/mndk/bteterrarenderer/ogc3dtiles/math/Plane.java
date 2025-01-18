package com.mndk.bteterrarenderer.ogc3dtiles.math;

import lombok.Getter;
import org.joml.Vector3d;

@Getter
public class Plane {
    private final Vector3d point;
    private final Vector3d normal;

    public Plane(Vector3d point, Vector3d normal) {
        this.point = point;
        this.normal = normal.normalize(new Vector3d());
    }
}
