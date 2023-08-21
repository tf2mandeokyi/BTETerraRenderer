package com.mndk.bteterrarenderer.core.ogc3d.math.volume;

import com.mndk.bteterrarenderer.core.ogc3d.math.Cartesian3;
import com.mndk.bteterrarenderer.core.ogc3d.math.Matrix3;
import com.mndk.bteterrarenderer.core.ogc3d.math.MatrixMajor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class Box extends Volume {
    // Read these from array in order
    private final Cartesian3 center;
    private final Matrix3 halfLength;

    @Override
    public boolean containsCartesian(Cartesian3 cartesian) {
        // TODO implement this
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean intersectsGeoCoordinate(double[] coordinate) {
        // TODO implement this
        throw new UnsupportedOperationException("Not implemented");
    }

    public static Box fromArray(double[] array) {
        return new Box(Cartesian3.fromArray(array, 0), Matrix3.fromArray(array, 3, MatrixMajor.ROW));
    }
}
