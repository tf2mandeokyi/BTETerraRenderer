package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Parallelepiped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3d;

@Getter
@RequiredArgsConstructor
public class AABB {
    private final Vector3d min, max;

    public AABB include(Vector3d point) {
        Vector3d min = this.min.min(point, new Vector3d());
        Vector3d max = this.max.max(point, new Vector3d());
        return new AABB(min, max);
    }

    public AABB include(AABB box) {
        Vector3d min = this.min.min(box.min, new Vector3d());
        Vector3d max = this.max.max(box.max, new Vector3d());
        return new AABB(min, max);
    }

    public Parallelepiped toBox() {
        Vector3d center = this.min.add(this.max, new Vector3d()).mul(0.5);
        Vector3d halfScale = this.max.sub(this.min, new Vector3d()).mul(0.5);
        Vector3d sx = new Vector3d(halfScale.x, 0, 0);
        Vector3d sy = new Vector3d(0, halfScale.y, 0);
        Vector3d sz = new Vector3d(0, 0, halfScale.z);
        return new Parallelepiped(center, sx, sy, sz);
    }

    public static AABB fromPoint(Vector3d point) {
        return new AABB(point, point);
    }

    @Override
    public String toString() {
        return "AABB{min=" + min + ", max=" + max + "}";
    }
}
