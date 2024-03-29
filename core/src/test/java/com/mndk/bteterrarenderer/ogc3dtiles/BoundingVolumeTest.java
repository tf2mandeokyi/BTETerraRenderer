package com.mndk.bteterrarenderer.ogc3dtiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mndk.bteterrarenderer.core.BTETerraRendererConstants;
import com.mndk.bteterrarenderer.ogc3dtiles.math.Cartesian3;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Box;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Region;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Sphere;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Volume;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
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
        Volume volume = BTETerraRendererConstants.JSON_MAPPER.readValue(json, Volume.class);
        MatcherAssert.assertThat(volume, CoreMatchers.instanceOf(Region.class));

        Region region = (Region) volume;
        Assert.assertEquals(region.getMaxHeight() - region.getMinHeight(), 20, 0.00001);
    }

    @Test
    public void givenSphereJson_testReadability() throws JsonProcessingException {
        String json = "{\"sphere\": [0, 0, 10, 141.4214]}";
        Volume volume = BTETerraRendererConstants.JSON_MAPPER.readValue(json, Volume.class);
        MatcherAssert.assertThat(volume, CoreMatchers.instanceOf(Sphere.class));
        Assert.assertEquals(((Sphere) volume).getRadius(), 141.4214, 0.00001);
    }

    @Test
    public void givenBoxJson_testReadability() throws JsonProcessingException {
        String json = "{\"box\": [\n" +
                "    0,   0,   10,\n" +
                "    100, 0,   0,\n" +
                "    0,   100, 0,\n" +
                "    0,   0,   10\n" +
                "  ]}";
        Volume volume = BTETerraRendererConstants.JSON_MAPPER.readValue(json, Volume.class);
        MatcherAssert.assertThat(volume, CoreMatchers.instanceOf(Box.class));
        Assert.assertEquals(((Box) volume).getCenter(), new Cartesian3(0, 0, 10));
    }

    @Test
    public void givenWrongSphereJson_testThrowable() {
        String json = "{\"sphere\": [0, 0, 10]}";
        Assert.assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> BTETerraRendererConstants.JSON_MAPPER.readValue(json, Volume.class));
    }
}
