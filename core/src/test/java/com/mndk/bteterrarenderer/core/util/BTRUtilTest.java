package com.mndk.bteterrarenderer.core.util;

import com.mndk.bteterrarenderer.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class BTRUtilTest {

    @Test
    public void testDoubleFormatter() {
        Assert.assertEquals("0.250", StringUtil.formatDoubleNicely(0.25, 3));
        Assert.assertEquals("0.500", StringUtil.formatDoubleNicely(0.5, 3));
        Assert.assertEquals("1", StringUtil.formatDoubleNicely(1, 3));
    }

}
