package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4f;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Box;
import org.junit.Assert;
import org.junit.Test;

public class BoxIntersectionTest {
    @Test
    public void givenBox_testRayIntersection() {
        Box box = Box.fromArray(new double[] {
                1.56, 7.71, 5,
                3.76, -1.15, 1.21,
                0.47, 4.22, 0,
                -0.72, -0.34, 2
        });
        Assert.assertTrue(box.intersectsRay(
                new Cartesian3f(8.51, 8.69, 4),
                new Cartesian3f(3.72, 4.79, 3.42),
                Matrix4f.IDENTITY
        ));
    }

    @Test
    public void givenBox_testUnitSphereIntersection() {
        Box box = Box.fromArray(new double[] {
                0.68, 1.49, 0.19,
                1.05, -0.71, 0.42,
                0.18, 0.74, 0,
                -0.14, 0, 0.84
        });
        Assert.assertTrue(UnitSphere.checkParallelepipedIntersection(box.boxMatrix));
    }
}
