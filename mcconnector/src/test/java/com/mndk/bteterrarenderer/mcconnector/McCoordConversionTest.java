package com.mndk.bteterrarenderer.mcconnector;

import com.mndk.bteterrarenderer.mcconnector.util.math.McCoord;
import org.junit.Assert;
import org.junit.Test;

public class McCoordConversionTest {

    private static final double EPSILON = 1e-6;

    @Test
    public void givenSpecialAngles_testMcCoordConversion() {
        // ==== MINECRAFT ROTATION SYSTEM ====
        // Z axis: yawDegrees = 0
        // -X axis: yawDegrees = 90
        // -Z axis: yawDegrees = 180
        // X axis: yawDegrees = 270
        // Y axis: pitchDegrees = -90
        // -Y axis: pitchDegrees = 90
        assertMcCoordEquals(new McCoord(0, 0, 1), McCoord.fromYawPitch(0, 0));
        assertMcCoordEquals(new McCoord(-1, 0, 0), McCoord.fromYawPitch(90, 0));
        assertMcCoordEquals(new McCoord(0, 0, -1), McCoord.fromYawPitch(180, 0));
        assertMcCoordEquals(new McCoord(1, 0, 0), McCoord.fromYawPitch(270, 0));
        assertMcCoordEquals(new McCoord(0, 1, 0), McCoord.fromYawPitch(0, -90));
        assertMcCoordEquals(new McCoord(0, -1, 0), McCoord.fromYawPitch(0, 90));
    }

    private static void assertMcCoordEquals(McCoord expected, McCoord actual) {
        Assert.assertEquals(expected.getX(), actual.getX(), EPSILON);
        Assert.assertEquals(expected.getY(), actual.getY(), EPSILON);
        Assert.assertEquals(expected.getZ(), actual.getZ(), EPSILON);
    }

}
