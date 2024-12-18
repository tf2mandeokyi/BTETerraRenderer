package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix3f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Box;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AABB {
    private final Cartesian3f min, max;

    public AABB include(Cartesian3f point) {
        Cartesian3f min = Cartesian3f.min(this.min, point);
        Cartesian3f max = Cartesian3f.max(this.max, point);
        return new AABB(min, max);
    }

    public AABB include(AABB box) {
        Cartesian3f min = Cartesian3f.min(this.min, box.min);
        Cartesian3f max = Cartesian3f.max(this.max, box.max);
        return new AABB(min, max);
    }

    public Box toBox() {
        Cartesian3f center = this.min.add(this.max).scale(0.5f);
        float[] halfScale = this.max.subtract(this.min).scale(0.5f).toArray();
        Matrix3f scale = new Matrix3f((c, r) -> c == r ? halfScale[c] : 0);
        return new Box(center, scale);
    }

    public static AABB fromPoint(Cartesian3f point) {
        return new AABB(point, point);
    }

    @Override
    public String toString() {
        return "AABB{min=" + min + ", max=" + max + "}";
    }
}
