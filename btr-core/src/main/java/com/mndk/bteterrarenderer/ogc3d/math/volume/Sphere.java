package com.mndk.bteterrarenderer.ogc3d.math.volume;

import com.mndk.bteterrarenderer.ogc3d.math.Cartesian3;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Sphere extends Volume {
    private final Cartesian3 center;
    private final double radius;

    @Override
    public boolean containsCartesian(Cartesian3 cartesian) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean intersectsGeoCoordinate(double[] coordinate) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public static Sphere fromArray(double[] array) {
        return new Sphere(Cartesian3.fromArray(array, 0), array[3]);
    }
}
