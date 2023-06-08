package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.config.BTRConfigConnector;
import org.junit.Assert;
import org.junit.Test;

public class Impl18Test {

    @Test
    public void testImplFinder() {
        Assert.assertEquals(BTRConfigConnector.INSTANCE.getMapServiceId(), "osm");
    }

}
