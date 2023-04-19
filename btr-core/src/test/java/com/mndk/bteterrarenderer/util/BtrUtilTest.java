package com.mndk.bteterrarenderer.util;

import org.junit.Assert;
import org.junit.Test;

public class BtrUtilTest {

    @Test
    public void testDoubleFormatter() {
        Assert.assertEquals(BtrUtil.formatDoubleNicely(0.25, 3), "0.250");
        Assert.assertEquals(BtrUtil.formatDoubleNicely(0.5, 3), "0.500");
        Assert.assertEquals(BtrUtil.formatDoubleNicely(1, 3), "1");
    }

}
