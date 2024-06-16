package com.mndk.bteterrarenderer.draco.core;

import lombok.Getter;

@Getter
public class BoundingBox {

    private final VectorD.F3 minPoint;
    private final VectorD.F3 maxPoint;

    public BoundingBox() {
        this.minPoint = new VectorD.F3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        this.maxPoint = new VectorD.F3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
    }

    public BoundingBox(VectorD.F3 minPoint, VectorD.F3 maxPoint) {
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public boolean isValid() {
        return minPoint.get(0) != Float.MAX_VALUE &&
               minPoint.get(1) != Float.MAX_VALUE &&
               minPoint.get(2) != Float.MAX_VALUE &&
               maxPoint.get(0) != Float.MIN_VALUE &&
               maxPoint.get(1) != Float.MIN_VALUE &&
               maxPoint.get(2) != Float.MIN_VALUE;
    }

    public void update(VectorD.F3 newPoint) {
        for (int i = 0; i < 3; i++) {
            if (newPoint.get(i) < minPoint.get(i)) {
                minPoint.set(i, newPoint.get(i));
            }
            if (newPoint.get(i) > maxPoint.get(i)) {
                maxPoint.set(i, newPoint.get(i));
            }
        }
    }

    public void update(BoundingBox other) {
        update(other.minPoint);
        update(other.maxPoint);
    }

    public VectorD.F3 size() {
        return maxPoint.subtract(minPoint);
    }

    public VectorD.F3 center() {
        return minPoint.add(maxPoint).divide(2f);
    }
}
