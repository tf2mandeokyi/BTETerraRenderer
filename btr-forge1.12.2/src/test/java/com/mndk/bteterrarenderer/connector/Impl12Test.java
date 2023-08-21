package com.mndk.bteterrarenderer.connector;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import org.junit.Assert;
import org.junit.Test;

public class Impl12Test {

    @Test
    public void testImplFinder() {
        Assert.assertEquals(BTETerraRendererConfig.INSTANCE.getMapServiceId(), "osm");
    }

}
