package com.mndk.bteterrarenderer.draco.compression.config;

import com.mndk.bteterrarenderer.draco.attributes.GeometryAttribute;
import org.junit.Assert;
import org.junit.Test;

public class DecoderOptionsTest {

    @Test
    public void testOptions() {
        // This test verifies that we can update global and attribute options of the
        // DecoderOptions class instance.
        DecoderOptions options = new DecoderOptions();
        options.setGlobalInt("test", 3);
        Assert.assertEquals(options.getGlobalInt("test", -1), 3);

        options.setAttributeInt(GeometryAttribute.Type.POSITION, "test", 1);
        options.setAttributeInt(GeometryAttribute.Type.GENERIC, "test", 2);
        Assert.assertEquals(
            options.getAttributeInt(GeometryAttribute.Type.TEX_COORD, "test", -1),
            3
        );
        Assert.assertEquals(
            options.getAttributeInt(GeometryAttribute.Type.POSITION, "test", -1),
            1
        );
        Assert.assertEquals(
            options.getAttributeInt(GeometryAttribute.Type.GENERIC, "test", -1),
            2
        );
    }

    @Test
    public void testAttributeOptionsAccessors() {
        // This test verifies that we can query options stored in DecoderOptions
        // class instance.
        DecoderOptions options = new DecoderOptions();
        options.setGlobalInt("test", 1);
        options.setAttributeInt(GeometryAttribute.Type.POSITION, "test", 2);
        options.setAttributeInt(GeometryAttribute.Type.TEX_COORD, "test", 3);

        Assert.assertEquals(
            options.getAttributeInt(GeometryAttribute.Type.POSITION, "test", -1),
            2
        );
        Assert.assertEquals(
            options.getAttributeInt(GeometryAttribute.Type.POSITION, "test2", -1),
            -1
        );
        Assert.assertEquals(
            options.getAttributeInt(GeometryAttribute.Type.TEX_COORD, "test", -1),
            3
        );
        Assert.assertEquals(
            options.getAttributeInt(GeometryAttribute.Type.NORMAL, "test", -1),
            1
        );
    }

}
