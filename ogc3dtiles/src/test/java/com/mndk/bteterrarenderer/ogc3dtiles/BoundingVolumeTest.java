package com.mndk.bteterrarenderer.ogc3dtiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.BTETerraRenderer;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Ellipsoid;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Parallelepiped;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Region;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.junit.Assert;
import org.junit.Test;

public class BoundingVolumeTest {

    @Test
    public void givenRegionJson_testReadability() throws JsonProcessingException {
        String json = "{\"region\": [-1.3197004795898053,\n" +
                "    0.6988582109,\n" +
                "    -1.3196595204101946,\n" +
                "    0.6988897891,\n" +
                "    0,\n" +
                "    20]}";
        Volume volume = BTETerraRenderer.JSON_MAPPER.readValue(json, Volume.class);
        MatcherAssert.assertThat(volume, CoreMatchers.instanceOf(Region.class));

        Region region = (Region) volume;
        Assert.assertEquals(20, region.getMaxHeight() - region.getMinHeight(), 0.00001);
    }

    @Test
    public void givenSphereJson_testReadability() throws JsonProcessingException {
        String json = "{\"sphere\": [0, 0, 10, 141.4214]}";
        Volume volume = BTETerraRenderer.JSON_MAPPER.readValue(json, Volume.class);
        MatcherAssert.assertThat(volume, CoreMatchers.instanceOf(Ellipsoid.class));
        Matrix3d radiusMatrix = ((Ellipsoid) volume).getRadiusMatrix();
        Assert.assertEquals(141.4214, Math.cbrt(radiusMatrix.determinant()), 0.00001);
    }

    @Test
    public void givenBoxJson_testReadability() throws JsonProcessingException {
        String json = "{\"box\": [\n" +
                "    0,   0,   10,\n" +
                "    100, 0,   0,\n" +
                "    0,   100, 0,\n" +
                "    0,   0,   10\n" +
                "  ]}";
        Volume volume = BTETerraRenderer.JSON_MAPPER.readValue(json, Volume.class);
        MatcherAssert.assertThat(volume, CoreMatchers.instanceOf(Parallelepiped.class));
        Assert.assertEquals(new Vector3d(0, 0, 10), ((Parallelepiped) volume).getCenter());
    }

    @Test
    public void givenWrongSphereJson_testThrowable() {
        String json = "{\"sphere\": [0, 0, 10]}";
        Assert.assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> BTETerraRenderer.JSON_MAPPER.readValue(json, Volume.class));
    }
}
