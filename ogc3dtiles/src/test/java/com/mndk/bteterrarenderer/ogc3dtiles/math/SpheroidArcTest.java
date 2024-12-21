package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.geoid.GeoidHeightFunction;
import org.junit.Test;

public class SpheroidArcTest {

    private static final SpheroidCoordinatesConverter ZERO_RADIUS_EARTH = new SpheroidCoordinatesConverter(
            3, 3, GeoidHeightFunction.WGS84_ELLIPSOID);

    @Test
    public void givenArc_testBoundingBox() {
        SpheroidArc arc = new SpheroidArc(0, Math.PI / 2, 0, 3);
        AABB box = arc.getBoundingBox(ZERO_RADIUS_EARTH);
        System.out.println(box);
    }

}
