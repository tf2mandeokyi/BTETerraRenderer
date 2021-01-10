package com.mndk.kmap4bte.projection.terra121;

import com.mndk.kmap4bte.projection.GeographicProjection;

public abstract class ProjectionTransform extends GeographicProjection {
    protected GeographicProjection input;

    public ProjectionTransform(GeographicProjection input) {
        this.input = input;
    }

    public boolean upright() {
        return input.upright();
    }

    public double[] bounds() {
        return input.bounds();
    }

    public double metersPerUnit() {
        return input.metersPerUnit();
    }
}