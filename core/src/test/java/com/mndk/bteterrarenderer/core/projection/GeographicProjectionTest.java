package com.mndk.bteterrarenderer.core.projection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.GeographicProjection;
import com.mndk.bteterrarenderer.dep.terraplusplus.projection.OutOfProjectionBoundsException;
import org.junit.Assert;
import org.junit.Test;

public class GeographicProjectionTest {

    public static final double[] GEO_COORD = new double[] { -73.98566440289457, 40.74843814459844 };
    public static final double[] GAME_COORD = new double[] { -8525873.069135161, -6026164.9710848285 };

    @Test
    public void givenJsonConfig_testBTEProjection() {
        validateBTEProjection(Projections.BTE);
    }

    @Test
    public void givenJsonConfig_testJsonSerialize() throws JsonProcessingException {
        String json = BTETerraRenderer.JSON_MAPPER.writeValueAsString(Projections.BTE);
        GeographicProjection projection = GeographicProjection.parse(json);
        validateBTEProjection(projection);
    }

    public static void validateBTEProjection(GeographicProjection projection) {
        try {
            Assert.assertArrayEquals(GAME_COORD, projection.fromGeo(GEO_COORD[0], GEO_COORD[1]), 0);
        } catch (OutOfProjectionBoundsException e) {
            throw new RuntimeException(e);
        }
    }

}
