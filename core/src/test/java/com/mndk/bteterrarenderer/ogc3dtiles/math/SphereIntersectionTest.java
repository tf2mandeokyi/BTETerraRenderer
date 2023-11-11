package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.Wgs84Constants;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Region;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Sphere;
import org.junit.Assert;
import org.junit.Test;

public class SphereIntersectionTest {
    @Test
    public void givenSphere_testBoundingBox() {
        double a = Wgs84Constants.SEMI_MAJOR_AXIS / 2;
        Sphere sphere = new Sphere(new Cartesian3(a, a, a*Math.sqrt(2)), a);
        Region region = sphere.toBoundingRegions()[0];

        Assert.assertEquals(0, Math.toDegrees(region.getWestLon()), 0.00001);
        Assert.assertEquals(90, Math.toDegrees(region.getEastLon()), 0.00001);
        Assert.assertEquals(60, Math.toDegrees(region.getNorthLat() - region.getSouthLat()), 0.00001);
        Assert.assertEquals(10719, (region.getMaxHeight() + region.getMinHeight()) / 2, 1);
    }
}
