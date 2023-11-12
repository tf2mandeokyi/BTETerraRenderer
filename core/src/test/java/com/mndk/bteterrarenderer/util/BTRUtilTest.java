package com.mndk.bteterrarenderer.util;

import com.mndk.bteterrarenderer.core.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class BTRUtilTest {

    @Test
    public void testDoubleFormatter() {
        Assert.assertEquals(StringUtil.formatDoubleNicely(0.25, 3), "0.250");
        Assert.assertEquals(StringUtil.formatDoubleNicely(0.5, 3), "0.500");
        Assert.assertEquals(StringUtil.formatDoubleNicely(1, 3), "1");
    }

}
