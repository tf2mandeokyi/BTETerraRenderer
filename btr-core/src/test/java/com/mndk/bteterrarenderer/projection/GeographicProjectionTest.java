package com.mndk.bteterrarenderer.projection;

import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import org.junit.Assert;
import org.junit.Test;

public class GeographicProjectionTest {

    @Test
    public void givenJsonConfig_testBTEProjection() throws OutOfProjectionBoundsException {
        final double[] geoCoordinate = new double[] { -73.98566440289457, 40.74843814459844 };
        final double[] gameCoordinate = new double[] { -8525873.069135161, -6026164.9710848285 };
        Assert.assertArrayEquals(gameCoordinate, Projections.BTE.fromGeo(geoCoordinate[0], geoCoordinate[1]), 0);
    }

}
