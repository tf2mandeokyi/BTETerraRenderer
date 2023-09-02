package com.mndk.bteterrarenderer.ogc3dtiles.math;

import com.mndk.bteterrarenderer.ogc3dtiles.math.matrix.Matrix4;
import com.mndk.bteterrarenderer.ogc3dtiles.math.volume.Box;
import org.junit.Assert;
import org.junit.Test;

public class ShapeIntersectionTest {
    @Test
    public void givenBox_testIntersection() {
        Box box = Box.fromArray(new double[] {
                1.56, 7.71, 5,
                3.76, -1.15, 1.21,
                0.47, 4.22, 0,
                -0.72, -0.34, 2
        });
        Assert.assertTrue(box.intersectsRay(
                new Cartesian3(8.51, 8.69, 4),
                new Cartesian3(3.72, 4.79, 3.42),
                Matrix4.IDENTITY
        ));
    }
}
