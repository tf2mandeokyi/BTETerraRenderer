package com.mndk.bteterrarenderer.core.tile.ogc3dtiles;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Spheroid3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.SpheroidCoordinatesConverter;
import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingleGltfModelParsingContext {

    private final Cartesian3 translation;
    private final Matrix4 transform;
    private final GeographicProjection projection;
    private final SpheroidCoordinatesConverter coordConverter;
    private final boolean rotateModelAlongEarthXAxis;

    public Cartesian3 transformEarthCoordToGame(Cartesian3 earthCartesian) throws OutOfProjectionBoundsException {
        Cartesian3 transformed = earthCartesian.add(this.translation).transform(this.transform);
        if(rotateModelAlongEarthXAxis) {
            transformed = new Cartesian3(transformed.getX(), -transformed.getZ(), transformed.getY());
        }

        Spheroid3 s3 = coordConverter.toSpheroid(transformed);
        double[] posXY = this.projection.fromGeo(s3.getLongitudeDegrees(), s3.getLatitudeDegrees());
        return new Cartesian3(posXY[0], s3.getHeight(), posXY[1]);
    }

}
