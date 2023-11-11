package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Region;
import org.junit.Assert;
import org.junit.Test;

public class RegionIntersectionTest {
    @Test
    public void givenIntersectingRegions_testIntersection() {
        Region region1 = new Region(-1, -0.5, 1, 0.5, 0, 1);
        Region region2 = new Region(-2, -1, 0, 0, 0, 1);
        Assert.assertTrue(region2.intersectsRegion(region1));
    }
}
